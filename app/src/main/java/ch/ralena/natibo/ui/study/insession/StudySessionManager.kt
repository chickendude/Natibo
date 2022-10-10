package ch.ralena.natibo.ui.study.insession

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LifecycleService
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.service.StudyServiceManager
import ch.ralena.natibo.settings.CourseSettings
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
	private val sessionRepository: SessionRepository,
	private val fetchSessionWithSentencesUseCase: FetchSessionWithSentencesUseCase,
	private val notificationHelper: NotificationHelper,
	@ApplicationContext private val applicationContext: Context,
	private val studyServiceManager: StudyServiceManager,
	private val courseSettings: CourseSettings,
	dispatchers: DispatcherProvider
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
		initMediaSession()
		notificationHelper.mediaSession = mediaSession
	}

	fun start(course: CourseRoom) {
		this.course = course
		if (mediaPlayer == null) setUpMediaPlayer()
		coroutineScope.launch {
			session = fetchSessionWithSentencesUseCase.fetchSessionWithSentences(course.sessionId)
				?: return@launch
			events.emit(Event.SessionLoaded)
			loadSentence()
			play()
			studyServiceManager.startService()
		}
	}

	fun stop() {
		studyState.value = StudyState.PAUSED
		mediaPlayer?.run {
			if (isPlaying) stop()
			release()
		}
		mediaPlayer = null
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

	private fun nextSentence() {
		coroutineScope.launch {
			session.nextSentence()
			loadSentence()
			sessionRepository.saveNatiboSession(session)
		}
	}

	fun nextSentencePair() {
		coroutineScope.launch {
			session.nextSentencePair()
			loadSentence()
			sessionRepository.saveNatiboSession(session)
		}
	}

	fun previousSentencePair() {
		coroutineScope.launch {
			session.previousSentencePair()
			loadSentence()
			sessionRepository.saveNatiboSession(session)
		}
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

	private suspend fun loadSentence() {
		currentSentence.value = session.currentSentencePair
		val sentence = session.currentSentence
		if (sentence == null) {
			studyState.value = StudyState.COMPLETE
			return
		}
		events.emit(Event.SentenceLoaded(sentence))

		mediaPlayer?.run {
			reset()
			setDataSource(sentence.mp3)
			setOnPreparedListener { }
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
					delay(courseSettings.delayBetweenSentences.get().toLong())
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
					nextSentencePair()
				}


				override fun onSkipToPrevious() {
					super.onSkipToPrevious()
					previousSentencePair()
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
