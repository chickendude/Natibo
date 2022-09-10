package ch.ralena.natibo.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer.OnCompletionListener
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.*
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.service.StudySessionViewModel.Event
import ch.ralena.natibo.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TODO: Improve the pause/play functionality.
 *  Playing after the audio has completed and before the next sentence is loaded will sometimes
 *  replay the current audio file.
 */
@AndroidEntryPoint
internal class StudySessionServiceKt : LifecycleService(), OnCompletionListener,
	OnAudioFocusChangeListener {
	// Media Session
	private var mediaSessionManager: MediaSessionManager? = null
	private var mediaSession: MediaSessionCompat? = null
	private var transportControls: MediaControllerCompat.TransportControls? = null

	@Inject
	lateinit var viewModel: StudySessionViewModel

	private var mediaPlayer: MediaPlayer? = null
	private var audioManager: AudioManager? = null
	private var isPlaying = false
	private var phoneStateListener: PhoneStateListener? = null
	private lateinit var telephonyManager: TelephonyManager

	// --- getters/setters ---
	private var notificationBuilder: NotificationCompat.Builder? = null

	private var currentSentence = MutableStateFlow<NatiboSentence?>(null)
	fun currentSentence() = currentSentence.asStateFlow()

	private var studyState = MutableStateFlow(StudyState.UNINITIALIZED)
	fun studyState() = studyState.asStateFlow()

	// given to clients that connect to the service
	var binder: StudyBinder = StudyBinder()

	// Broadcast Receivers
	private val becomingNoisyReceiver: BroadcastReceiver = BecomingNoisyReceiver()
	private val startSessionReceiver: BroadcastReceiver = StartSessionReceiver()

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		// check if we have attached our bundle or not
		if (intent?.extras == null) stopSelf()

		val courseId = Utils.Storage(applicationContext).courseId
		viewModel.start(courseId)

		if (!requestAudioFocus()) stopSelf()

		lifecycleScope.launch {
			viewModel.events()
				.collect { event ->
					when (event) {
						is Event.SessionFinished -> studyState.value = StudyState.COMPLETE
						is Event.SessionLoaded -> studyState.value = StudyState.PLAYING
						is Event.SentenceLoaded -> loadSentence(event.sentence)
					}
				}
		}


		if (mediaSessionManager == null) {
			initMediaSession()
		}
		setUpMediaPlayer()
		handleIncomingActions(intent)
		buildNotification()
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onCreate() {
		super.onCreate()
		callStateListener()
		registerBecomingNoisyReceiver()
		registerStartSessionReceiver()
	}

	override fun onDestroy() {
		super.onDestroy()

		// stop media from playing
		if (mediaPlayer != null) {
			stop()
			mediaPlayer!!.release()
		}
		removeAudioFocus()

		// cancel the phone state listener
		if (phoneStateListener != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
		}
		removeNotification()

		// unregister broadcast receivers
		unregisterReceiver(becomingNoisyReceiver)
		unregisterReceiver(startSessionReceiver)
	}

	override fun onCompletion(mp: MediaPlayer) {
		lifecycleScope.launch {
			// TODO: Use settings to determine delay
			delay(1000)
			studyState.first { it == StudyState.PLAYING }
			viewModel.nextSentence()
		}
	}

	override fun onAudioFocusChange(focusChange: Int) {
		// when another app makes focus request
		when (focusChange) {
			AudioManager.AUDIOFOCUS_GAIN -> if (isPlaying) restartPlaying()
			AudioManager.AUDIOFOCUS_LOSS -> {
				isPlaying = false
				pauseAndRelease()
				buildNotification()
			}
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
				isPlaying = studyState.value == StudyState.PLAYING
				pause()
			}
		}
	}

	override fun onBind(intent: Intent): IBinder = binder

	private fun setUpMediaPlayer() {
		if (mediaPlayer == null) {
			mediaPlayer = MediaPlayer().apply {
				setOnCompletionListener(this@StudySessionServiceKt)
				setAudioStreamType(AudioManager.STREAM_MUSIC)
			}
		}
	}

	// --- setup ---
	private fun loadSentence(sentence: SentenceRoom) {
		currentSentence.value = viewModel.currentSentence

		mediaPlayer?.apply {
			reset()
			setDataSource(sentence.mp3)
			prepare()
			// TODO: Handle speed from settings
			playbackParams = playbackParams.setSpeed(1f)
		}
		if (studyState.value == StudyState.PLAYING)
			play()
		else {
			studyState.value = StudyState.READY
		}
		updateNotificationText()
	}

	// --- managing media ---
	private fun initMediaSession() {
		if (mediaSessionManager != null) return

		mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
		mediaSession = MediaSessionCompat(applicationContext, "Natibo").apply {
			transportControls = controller.transportControls
			isActive = true
			setCallback(object : MediaSessionCompat.Callback() {
				override fun onPlay() {
					super.onPlay()
					resume()
					buildNotification()
				}

				override fun onPause() {
					super.onPause()
					pause()
					buildNotification()
				}

				override fun onSkipToNext() {
					super.onSkipToNext()
					viewModel.nextSentence()
					buildNotification()
				}

				override fun onSkipToPrevious() {
					super.onSkipToPrevious()
					previousSentence()
					buildNotification()
				}
			})
		}
	}

	private fun updateNotificationText() {
		mediaSession?.setMetadata(
			MediaMetadataCompat.Builder()
				.putString(
					MediaMetadata.METADATA_KEY_TITLE,
					currentSentence.value?.native?.original
				)
				.putString(
					MediaMetadata.METADATA_KEY_ALBUM,
					currentSentence.value?.target?.original
				)
				.build()
		)
		if (notificationBuilder != null) {
			notificationBuilder!!
				.setContentText(currentSentence.value?.native?.original)
				.setContentTitle(currentSentence.value?.target?.original)
				.setOngoing(studyState.value == StudyState.PLAYING)
			val notificationManager: NotificationManager =
				getSystemService(NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder!!.build())
		}
	}

	private fun play() {
		if (studyState.value == StudyState.READY) mediaPlayer?.prepare()
		studyState.value = StudyState.PLAYING
		if (mediaPlayer?.isPlaying == false) {
			mediaPlayer?.start()
		}
	}

	private fun stop() {
		studyState.value = StudyState.PAUSED
		if (mediaPlayer?.isPlaying == true) {
			mediaPlayer?.stop()
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
	}

	private fun previousSentence() {
		// TODO: Switch to previous sentence
		viewModel.nextSentence()
		if (studyState.value == StudyState.PLAYING) {
			play()
		}
	}

	private fun setVolume(volume: Float) {
		if (mediaPlayer?.isPlaying == true) {
			mediaPlayer?.setVolume(volume, volume)
		}
	}

	// --- notification ---
	fun buildNotification() {
//		if (sentenceGroup == null) {
//			finishPublish.onNext(day)
//			return
//		}
		var playPauseDrawable = android.R.drawable.ic_media_pause
		var playPauseAction: PendingIntent? = null
		if (studyState.value == StudyState.PAUSED) {
			playPauseDrawable = android.R.drawable.ic_media_play
			playPauseAction = iconAction(ACTION_ID_PLAY)
		} else {
			playPauseDrawable = android.R.drawable.ic_media_pause
			playPauseAction = iconAction(ACTION_ID_PAUSE)
		}

		// create the notification channel
		val notificationManager: NotificationManager =
			getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// Create the NotificationChannel, but only on API 26+ because
			// the NotificationChannel class is new and not in the support library
			val name: CharSequence = "Study Session"
			val description = "Displays your sentences for a study session."
			val importance: Int = NotificationManager.IMPORTANCE_LOW
			val channel = NotificationChannel(CHANNEL_ID, name, importance)
			channel.setDescription(description)
			// Register the channel with the system
			notificationManager.createNotificationChannel(channel)
		}


		// create the notification
//		val activityIntent = Intent(this, MainActivity::class.java)
//				PendingIntent contentIntent = PendingIntent.getActivity(this, MainActivity.REQUEST_LOAD_SESSION, activityIntent, 0);
		notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //				.setContentIntent(contentIntent)
			.setShowWhen(false)
			.setOngoing(studyState.value == StudyState.PLAYING)
			.setOnlyAlertOnce(true)
			.setSmallIcon(R.drawable.ic_logo)
			.setColorized(false)
			.setStyle(
				androidx.media.app.NotificationCompat.MediaStyle()
					.setMediaSession(mediaSession!!.sessionToken)
					.setShowActionsInCompactView(0, 1, 2)
			)
			.setContentText(currentSentence.value?.native?.original)
			.setContentTitle(currentSentence.value?.target?.original)
			.addAction(
				android.R.drawable.ic_media_previous,
				"prev sentence",
				iconAction(ACTION_ID_PREVIOUS)
			)
			.addAction(playPauseDrawable, "pause", playPauseAction)
			.addAction(
				android.R.drawable.ic_media_next,
				"next sentence",
				iconAction(ACTION_ID_NEXT)
			)
		val notification = notificationBuilder!!.build()
		notificationManager.notify(NOTIFICATION_ID, notification)
		startForeground(NOTIFICATION_ID, notification)
	}

	fun removeNotification() {
		val notificationManager: NotificationManager =
			getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(NOTIFICATION_ID)
		stopForeground(true)
		notificationBuilder = null
	}

	private fun iconAction(actionId: Int): PendingIntent? {
		val iconIntent = Intent(this, StudySessionServiceKt::class.java)
		when (actionId) {
			ACTION_ID_PLAY -> iconIntent.action = ACTION_PLAY
			ACTION_ID_PAUSE -> iconIntent.action = ACTION_PAUSE
			ACTION_ID_NEXT -> iconIntent.action = ACTION_NEXT
			ACTION_ID_PREVIOUS -> iconIntent.action = ACTION_PREVIOUS
			else -> return null
		}
		return PendingIntent.getService(this, actionId, iconIntent, 0)
	}

	private fun handleIncomingActions(playbackAction: Intent?) {
		if (playbackAction?.action == null) return

		val actionString = playbackAction.action
		when (actionString?.lowercase()) {
			ACTION_PLAY -> transportControls!!.play()
			ACTION_PAUSE -> transportControls!!.pause()
			ACTION_NEXT -> transportControls!!.skipToNext()
			ACTION_PREVIOUS -> transportControls!!.skipToPrevious()
		}
	}

	// --- call state listener
	private fun callStateListener() {
		if (!isReadPhoneStateGranted()) return

		telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			telephonyManager.registerTelephonyCallback(
				mainExecutor,
				object : TelephonyCallback(), TelephonyCallback.CallStateListener {
					override fun onCallStateChanged(state: Int) {
						callStateChanged(state)
					}
				})
		} else {
			telephonyManager.listen(object : PhoneStateListener() {
				@Deprecated("Deprecated in Java")
				override fun onCallStateChanged(state: Int, phoneNumber: String?) {
					callStateChanged(state)
				}
			}, PhoneStateListener.LISTEN_CALL_STATE)
		}
	}

	private fun callStateChanged(state: Int) {
		when (state) {
			TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
				// phone ringing or in phone call
				isPlaying = studyState.value == StudyState.PLAYING
				pause()
			}
			TelephonyManager.CALL_STATE_IDLE ->
				// back from phone call
				if (isPlaying) resume()
		}
	}

	private fun requestAudioFocus(): Boolean {
		audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
		val result = audioManager?.requestAudioFocus(
			this,
			AudioManager.STREAM_MUSIC,
			AudioManager.AUDIOFOCUS_GAIN
		)
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
	}

	private fun removeAudioFocus(): Boolean {
		return audioManager?.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
	}

	private fun pauseAndRelease() {
		pause()
		mediaPlayer?.release()
		mediaPlayer = null
	}

	private fun restartPlaying() {
		if (mediaPlayer == null) {
			// TODO: Maybe incorrect
			viewModel.nextSentence()
		}
		play()

		// restore full volume levels
		setVolume(1.0f)
	}

	private fun isReadPhoneStateGranted() = ActivityCompat.checkSelfPermission(
		applicationContext,
		Manifest.permission.READ_PHONE_STATE
	) == PackageManager.PERMISSION_GRANTED


	// get a copy of the service so we can run its methods from fragment
	inner class StudyBinder : Binder() {
		val service: StudySessionServiceKt
			get() = this@StudySessionServiceKt
	}

	// --- Broadcast Receivers ---
	private fun registerBecomingNoisyReceiver() {
		val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
		registerReceiver(becomingNoisyReceiver, intentFilter)
	}

	private fun registerStartSessionReceiver() {
		val intentFilter = IntentFilter(BROADCAST_START_SESSION)
		registerReceiver(startSessionReceiver, intentFilter)
	}

	private inner class BecomingNoisyReceiver : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.getAction()) {
				pause()
				buildNotification()
			}
		}
	}

	private inner class StartSessionReceiver : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (!requestAudioFocus()) stopSelf()
		}
	}

	companion object {
		const val BROADCAST_START_SESSION = "broadcast_start_session"
		const val ACTION_PLAY = "action_play"
		const val ACTION_ID_PLAY = 0
		const val ACTION_PAUSE = "action_pause"
		const val ACTION_ID_PAUSE = 1
		const val ACTION_PREVIOUS = "action_previous"
		const val ACTION_ID_PREVIOUS = 2
		const val ACTION_NEXT = "action_next"
		const val ACTION_ID_NEXT = 3
		private const val NOTIFICATION_ID = 1337
		private const val CHANNEL_ID = "Natibo Study Notification"
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
