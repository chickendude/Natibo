package ch.ralena.natibo.ui.study.insession

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media.AudioManagerCompat.requestAudioFocus
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.usecases.data.FetchSessionWithSentencesUseCase
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.NotificationHelper
import ch.ralena.natibo.utils.STUDY_SESSION_NOTIFICATION_ID
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val TAG = StudySessionManager::class.simpleName

@Singleton
internal class StudySessionManager @Inject constructor(
	private val courseRepository: CourseRepository,
	private val fetchSessionWithSentencesUseCase: FetchSessionWithSentencesUseCase,
	private val notificationHelper: NotificationHelper,
	@ApplicationContext private val applicationContext: Context,
	dispatcherProvider: DispatcherProvider
) {
	// Media Session
	private var mediaSession: MediaSessionCompat? = null
	private var transportControls: MediaControllerCompat.TransportControls? = null

	private var mediaPlayer: MediaPlayer? = null
	private var audioManager: AudioManager? = null
	lateinit var session: NatiboSession

	private val job = Job()
	private val coroutineScope = CoroutineScope(job + dispatcherProvider.default())

	private val events = MutableSharedFlow<Event>()
	fun events() = events.asSharedFlow()

	private var currentSentence = MutableStateFlow<NatiboSentence?>(null)
	fun currentSentence() = currentSentence.asStateFlow()

	private var studyState = MutableStateFlow(StudyState.UNINITIALIZED)
	fun studyState() = studyState.asStateFlow()

	fun start(courseId: Long) {
		coroutineScope.launch {
			val result = courseRepository.fetchCourse(courseId)
			when (result) {
				is Result.Success -> loadSession(result.data)
				else -> Log.e(TAG, "Unable to load course with id $courseId")
			}
		}
		setUpMediaPlayer()
		initMediaSession()
	}

	fun stop() {
		studyState.value = StudyState.PAUSED
		mediaPlayer?.run {
			release()
			if (isPlaying) stop()
		}
		removeAudioFocus()
		notificationHelper.removeNotification()
	}

	private fun play() {
//		if (studyState.value == StudyState.READY) mediaPlayer?.prepare()
		studyState.value = StudyState.PLAYING
		if (mediaPlayer?.isPlaying == false) {
			mediaPlayer?.start()
		}
	}

	fun pause() {
		studyState.value = StudyState.PAUSED
		mediaPlayer?.pause()
	}

	fun resume() {
		if (requestAudioFocus()) {
			play()
		}
	}

	fun togglePausePlay() {
		if (studyState.value == StudyState.PLAYING) pause()
		else resume()
		notificationHelper.updateStudySessionNotification(
			studyState.value,
			mediaSession!!,
			currentSentence.value!!
		)
	}

	private fun loadSentence(sentence: SentenceRoom) {
		mediaPlayer?.apply {
			reset()
			setDataSource(sentence.mp3)
			prepare()
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				// TODO: Handle speed from settings
				playbackParams = playbackParams.setSpeed(1f)
			}
		}
		if (studyState.value == StudyState.PLAYING)
			play()
		else {
			studyState.value = StudyState.READY
		}
		notificationHelper.updateStudySessionNotification(
			studyState.value,
			mediaSession!!,
			currentSentence.value!!
		)
	}

	fun nextSentence() {
		coroutineScope.launch {
			session.nextSentence()
			currentSentence.value = session.currentSentencePair
			val sentence = session.currentSentence
			if (sentence == null) events.emit(Event.SessionFinished)
			else {
				loadSentence(sentence)
				events.emit(Event.SentenceLoaded(sentence))
			}
		}
	}

	fun previousSentence() {
		// TODO: Not implemented yet
	}

	private fun setUpMediaPlayer() {
		mediaPlayer = mediaPlayer ?: MediaPlayer().apply {
			setAudioStreamType(AudioManager.STREAM_MUSIC)
			setOnCompletionListener {
				coroutineScope.launch {
					// TODO: Use settings to determine delay
					delay(1000)
					studyState.first { it == StudyState.PLAYING }
					nextSentence()
				}
			}
		}
	}

	private fun restartPlaying() {
		if (mediaPlayer == null) {
			// TODO: Maybe incorrect
			nextSentence()
		}
		play()

		// restore full volume levels
		setVolume(1.0f)
	}

	private fun setVolume(volume: Float) = mediaPlayer?.run {
		if (isPlaying) setVolume(volume, volume)
	}

	private fun removeAudioFocus(): Boolean {
		return audioManager?.abandonAudioFocus(audioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
	}

	private suspend fun loadSession(course: CourseRoom) {
		session =
			fetchSessionWithSentencesUseCase.fetchSessionWithSentences(course.sessionId) ?: return
		events.emit(Event.SessionLoaded)
		nextSentence()
		play()
	}

	private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
		when (focusChange) {
			AudioManager.AUDIOFOCUS_GAIN ->
				if (studyState.value == StudyState.PLAYING) restartPlaying()
			AudioManager.AUDIOFOCUS_LOSS -> stop()
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> pause()
		}
	}

	private fun requestAudioFocus(): Boolean {
		audioManager =
			applicationContext.getSystemService(LifecycleService.AUDIO_SERVICE) as AudioManager
		val result = audioManager?.requestAudioFocus(
			audioFocusChangeListener,
			AudioManager.STREAM_MUSIC,
			AudioManager.AUDIOFOCUS_GAIN
		)
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
	}

	private fun initMediaSession() {
		mediaSession = MediaSessionCompat(applicationContext, "Natibo").apply {
			transportControls = controller.transportControls
			isActive = true
			setCallback(object : MediaSessionCompat.Callback() {
				override fun onPlay() {
					super.onPlay()
					resume()
				}

				override fun onPause() {
					super.onPause()
					pause()
				}

				override fun onSkipToNext() {
					super.onSkipToNext()
					nextSentence()
				}

				override fun onSkipToPrevious() {
					super.onSkipToPrevious()
					previousSentence()
				}
			})
		}
	}

	internal sealed class Event {
		data class SentenceLoaded(val sentence: SentenceRoom) : Event()
		object SessionLoaded : Event()
		object SessionFinished : Event()
	}
}

internal enum class StudyState {
	/** Session has not been initialized. */
	UNINITIALIZED,

	/** Playback is currently paused. */
	PAUSED,

	/** Audio is currently playing. */
	PLAYING,

	/** Sentence is loaded but audio has not been prepared. */
	READY,

	/** Session has completed. */
	COMPLETE
}
