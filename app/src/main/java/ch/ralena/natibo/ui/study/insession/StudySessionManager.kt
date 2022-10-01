package ch.ralena.natibo.ui.study.insession

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LifecycleService
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.service.StudyServiceManager
import ch.ralena.natibo.usecases.data.FetchSessionWithSentencesUseCase
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.NotificationHelper
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
	private val sessionRepository: SessionRepository,
	private val fetchSessionWithSentencesUseCase: FetchSessionWithSentencesUseCase,
	private val notificationHelper: NotificationHelper,
	@ApplicationContext private val applicationContext: Context,
	private val studyServiceManager: StudyServiceManager,
	private val dispatchers: DispatcherProvider
) {
	private val mediaSession = MediaSessionCompat(applicationContext, "Natibo")

	private var mediaPlayer: MediaPlayer? = null
	private var audioManager: AudioManager? = null
	lateinit var session: NatiboSession
	lateinit var course: CourseRoom

	private val job = Job()
	private val coroutineScope = CoroutineScope(job + dispatchers.default())

	private val events = MutableSharedFlow<Event>()
	fun events() = events.asSharedFlow()

	private var currentSentence = MutableStateFlow<NatiboSentence?>(null)
	fun currentSentence() = currentSentence.asStateFlow()

	private var studyState = MutableStateFlow(StudyState.UNINITIALIZED)
	fun studyState() = studyState.asStateFlow()

	init {
		setUpMediaPlayer()
		initMediaSession()
		notificationHelper.mediaSession = mediaSession
	}

	fun start(course: CourseRoom) {
		this.course = course
		coroutineScope.launch {
			session = fetchSessionWithSentencesUseCase.fetchSessionWithSentences(course.sessionId)
				?: return@launch
			events.emit(Event.SessionLoaded)
			nextSentence()
			studyServiceManager.startService()
		}
	}

	fun stop() {
		studyState.value = StudyState.PAUSED
		mediaPlayer?.run {
			release()
			if (isPlaying) stop()
		}
		removeAudioFocus()
		notificationHelper.removeNotification()
		studyServiceManager.stopService()
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
			currentSentence.value!!
		)
	}

	fun nextSentence() {
		coroutineScope.launch {
			session.nextSentence()
			currentSentence.value = session.currentSentencePair
			val sentence = session.currentSentence
			if (sentence == null) studyState.value = StudyState.COMPLETE
			else {
				loadSentence(sentence)
				events.emit(Event.SentenceLoaded(sentence))
			}
			sessionRepository.saveNatiboSession(session)
		}
	}

	fun previousSentence() {
		// TODO: Not implemented yet
	}

	fun finishSession() {
		coroutineScope.launch {
			sessionRepository.finishSession(session.sessionId)
			studyState.value = StudyState.UNINITIALIZED
			currentSentence.value = null
		}
	}

	private fun play() {
		studyState.value = StudyState.PLAYING
		mediaPlayer?.start()
	}

	private fun loadSentence(sentence: SentenceRoom) {
		mediaPlayer?.apply {
			reset()
			setDataSource(sentence.mp3)
			prepare()
			setOnPreparedListener { play() }
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
			currentSentence.value!!
		)
	}

	private fun setUpMediaPlayer() {
		mediaPlayer = mediaPlayer ?: MediaPlayer().apply {
			setAudioAttributes(
				AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
					.build()
			)
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
		// TODO: Maybe incorrect
//		play()

		// restore full volume levels
		setVolume(1.0f)
	}

	private fun setVolume(volume: Float) = mediaPlayer?.run {
		if (isPlaying) setVolume(volume, volume)
	}

	private fun removeAudioFocus(): Boolean {
		return audioManager?.abandonAudioFocus(audioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
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
		mediaSession.apply {
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
					// TODO: Skip to next sentence pair, not next sentence
					//  Currently, it'll go from English -> Tagalog, we want English#1 to English#2
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
