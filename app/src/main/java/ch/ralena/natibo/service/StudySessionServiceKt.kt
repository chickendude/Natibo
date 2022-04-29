package ch.ralena.natibo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer.OnCompletionListener
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.session.MediaSession
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Day
import ch.ralena.natibo.data.room.`object`.Sentence
import ch.ralena.natibo.data.room.`object`.SentenceGroup
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.utils.Utils
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import java.io.IOException
import java.lang.IllegalStateException

class StudySessionServiceKt : Service(), OnCompletionListener, OnAudioFocusChangeListener {
	// Media Session
	private var mediaSessionManager: MediaSessionManager? = null
	private var mediaSession: MediaSession? = null
	private var transportControls: MediaController.TransportControls? = null

	enum class PlaybackStatus {
		PLAYING, PAUSED
	}

	private var mediaPlayer: MediaPlayer? = null
	private var course: Course? = null
	private var day: Day? = null
	private var audioManager: AudioManager? = null
	private var isPlaying = false
	private var phoneStateListener: PhoneStateListener? = null
	private lateinit var telephonyManager: TelephonyManager
	private var realm: Realm? = null
	private var sentenceGroup: SentenceGroup? = null
	private var sentence: Sentence? = null

	// --- getters/setters ---
	var playbackStatus: PlaybackStatus? = null
		private set
	private var notificationBuilder: NotificationCompat.Builder? = null
	var sentencePublish: PublishSubject<SentenceGroup> = PublishSubject.create<SentenceGroup>()
	var finishPublish: PublishSubject<Day> = PublishSubject.create<Day>()

	// given to clients that connect to the service
	var binder: StudyBinder = StudyBinder()

	// Broadcast Receivers
	private val becomingNoisyReceiver: BroadcastReceiver = BecomingNoisyReceiver()
	private val startSessionReceiver: BroadcastReceiver = StartSessionReceiver()
	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		// check if we have attached our bundle or not
		if (intent.extras == null) stopSelf()

		realm = Realm.getDefaultInstance()
		val dayId = Utils.Storage(applicationContext).dayId
		if (day == null) {
//			day = realm.where<Day>(Day::class.java).equalTo("id", dayId).findFirst()
			if (day == null) stopSelf()
		}
		val courseId = Utils.Storage(applicationContext).courseId
		if (course == null) {
//			course = realm.where<Course>(Course::class.java).equalTo("id", courseId).findFirst()
			if (course == null) stopSelf()
		}
		if (!requestAudioFocus()) stopSelf()
		if (mediaPlayer == null) {
			loadSentence()
			play()
		}
		if (mediaSessionManager == null) {
			initMediaSession()
		}
		handleIncomingActions(intent)
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

	// --- setup ---
	private fun loadSentence() {
		sentenceGroup = day?.getCurrentSentenceGroup()

		// if sentenceGroup is null, we're done studying for the day!
		if (sentenceGroup == null) {
			removeNotification()
//			finishPublish.onNext(day)
			stop()
			stopSelf()
		} else {
//			sentencePublish.onNext(sentenceGroup)
//			sentence = day.getCurrentSentence()
			if (mediaPlayer == null) {
				mediaPlayer = MediaPlayer()
				mediaPlayer!!.setOnCompletionListener(this)
				mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
			}
			try {
				mediaPlayer!!.stop()
				mediaPlayer!!.reset()
				// load sentence path into mediaplayer to be played
//				mediaPlayer.setDataSource(sentence.getUri())

				// Set playback speed for the target language according to preferences
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					// Only change playback speed for the target language
//					if (sentence.getId() == sentenceGroup.getSentences().get(1).getId()) {
//						mediaPlayer!!.playbackParams =
//							mediaPlayer!!.playbackParams.setSpeed(course.getPlaybackSpeed())
//					} else {
//						mediaPlayer!!.playbackParams = mediaPlayer!!.playbackParams.setSpeed(1f)
//					}
				}
				mediaPlayer!!.prepare()
			} catch (e: IOException) {
				e.printStackTrace()
				stopSelf()
			} catch (e: IllegalStateException) {
				e.printStackTrace()
				stopSelf()
			}
			updateNotificationText()
		}
	}

	// --- managing media ---
	private fun initMediaSession() {
		if (mediaSessionManager != null) return

		mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
		mediaSession = MediaSession(applicationContext, "Natibo")
		transportControls = mediaSession!!.controller.transportControls
		mediaSession!!.isActive = true
		mediaSession!!.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
		mediaSession!!.setCallback(object : MediaSession.Callback() {
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
				nextSentence()
				buildNotification()
			}

			override fun onSkipToPrevious() {
				super.onSkipToPrevious()
				previousSentence()
				buildNotification()
			}

			override fun onSeekTo(pos: Long) {
				super.onSeekTo(pos)
			}
		})
	}

	private fun updateNotificationText() {
		if (notificationBuilder != null) {
//			notificationBuilder!!
//				.setContentText(sentenceGroup.getSentences().first().getText())
//				.setContentTitle(sentenceGroup.getSentences().last().getText())
//				.setOngoing(playbackStatus == PlaybackStatus.PLAYING)
			val notificationManager: NotificationManager =
				getSystemService(NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder!!.build())
		}
	}

	private fun play() {
		playbackStatus = PlaybackStatus.PLAYING
		if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
			mediaPlayer!!.start()
		}
	}

	private fun stop() {
		playbackStatus = PlaybackStatus.PAUSED
		if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
			mediaPlayer!!.stop()
		}
	}

	fun pause() {
		playbackStatus = PlaybackStatus.PAUSED
		if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
			mediaPlayer!!.pause()
		}
	}

	fun resume() {
		if (requestAudioFocus()) {
			playbackStatus = PlaybackStatus.PLAYING
			if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
				mediaPlayer!!.start()
			} else {
				loadSentence()
				play()
			}
		}
	}

	private fun nextSentence() {
//		day.goToNextSentencePair(realm)
		loadSentence()
		if (playbackStatus == PlaybackStatus.PLAYING) {
			play()
		}
	}

	private fun previousSentence() {
//		day.goToPreviousSentencePair(realm)
		loadSentence()
		if (playbackStatus == PlaybackStatus.PLAYING) {
			play()
		}
	}

	private fun setVolume(volume: Float) {
		if (mediaPlayer!!.isPlaying) {
			mediaPlayer!!.setVolume(volume, volume)
		}
	}

	// --- notification ---
	fun buildNotification() {
		if (sentenceGroup == null) {
//			finishPublish.onNext(day)
			return
		}
		var playPauseDrawable = android.R.drawable.ic_media_pause
		var playPauseAction: PendingIntent? = null
		if (playbackStatus == PlaybackStatus.PLAYING) {
			playPauseDrawable = android.R.drawable.ic_media_pause
			playPauseAction = iconAction(ACTION_ID_PAUSE)
		} else if (playbackStatus == PlaybackStatus.PAUSED) {
			playPauseDrawable = android.R.drawable.ic_media_play
			playPauseAction = iconAction(ACTION_ID_PLAY)
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
		val activityIntent = Intent(this, MainActivity::class.java)
		//		PendingIntent contentIntent = PendingIntent.getActivity(this, MainActivity.REQUEST_LOAD_SESSION, activityIntent, 0);
		notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //				.setContentIntent(contentIntent)
			.setShowWhen(false)
			.setOngoing(playbackStatus == PlaybackStatus.PLAYING)
			.setOnlyAlertOnce(true)
			.setSmallIcon(R.drawable.ic_logo)
			.setColorized(false)
			.setStyle(
				androidx.media.app.NotificationCompat.MediaStyle()
					.setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession!!.sessionToken))
					.setShowActionsInCompactView(1)
			)
//			.setContentText(sentenceGroup.getSentences().first().getText())
//			.setContentTitle(sentenceGroup.getSentences().last().getText())
			.addAction(
				android.R.drawable.ic_media_previous,
				"prev sentence",
				iconAction(ACTION_ID_PREVIOUS)
			)
			.addAction(playPauseDrawable, "pause", playPauseAction)
			.addAction(android.R.drawable.ic_media_next, "next sentence", iconAction(ACTION_ID_NEXT))
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
			ACTION_ID_PLAY -> iconIntent.setAction(ACTION_PLAY)
			ACTION_ID_PAUSE -> iconIntent.setAction(ACTION_PAUSE)
			ACTION_ID_NEXT -> iconIntent.setAction(ACTION_NEXT)
			ACTION_ID_PREVIOUS -> iconIntent.setAction(ACTION_PREVIOUS)
			else -> return null
		}
		return PendingIntent.getService(this, actionId, iconIntent, 0)
	}

	private fun handleIncomingActions(playbackAction: Intent?) {
		if (playbackAction?.action == null) return

		val actionString = playbackAction.action
		when {
			actionString.equals(ACTION_PLAY, ignoreCase = true) -> transportControls!!.play()
			actionString.equals(ACTION_PAUSE, ignoreCase = true) -> transportControls!!.pause()
			actionString.equals(ACTION_NEXT, ignoreCase = true) -> transportControls!!.skipToNext()
			actionString.equals(ACTION_PREVIOUS, ignoreCase = true) ->
				transportControls!!.skipToPrevious()
		}
	}

	// --- call state listener
	private fun callStateListener() {
		telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
		phoneStateListener = object : PhoneStateListener() {
			override fun onCallStateChanged(state: Int, incomingNumber: String) {
				when (state) {
					TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
						// phone ringing or in phone call
						isPlaying = playbackStatus == PlaybackStatus.PLAYING
						pause()
					}
					TelephonyManager.CALL_STATE_IDLE ->                        // back from phone call
						if (isPlaying) resume()
				}
			}
		}
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
	}

	override fun onBind(intent: Intent): IBinder? {
		return binder
	}

	override fun onCompletion(mp: MediaPlayer) {
		// when file has completed playing
//		if (day.nextSentence(realm)) {
//			val handler = Handler()
//			val runnable = Runnable {
//				loadSentence()
//				if (playbackStatus == PlaybackStatus.PLAYING) play()
//			}
//			handler.postDelayed(runnable, course.getPauseMillis().toLong())
//		}
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
				isPlaying = playbackStatus == PlaybackStatus.PLAYING
				pause()
			}
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
		stop()
		mediaPlayer!!.release()
		mediaPlayer = null
	}

	private fun restartPlaying() {
		if (mediaPlayer == null) {
			loadSentence()
		}
		play()

		// restore full volume levels
		setVolume(1.0f)
	}

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
			val id = Utils.Storage(applicationContext).dayId
//			day = realm.where<Day>(Day::class.java).equalTo("id", id).findFirst()
			if (day == null) stopSelf()
			if (!requestAudioFocus()) stopSelf()

			// if the app is playing, we don't need to reload the sentence.
			// if nothing is playing, we'll need to load the sentence and start it.
//			if (playbackStatus != PlaybackStatus.PLAYING && !day.isCompleted()) {
//				loadSentence()
//				play()
//			}

			// when returning to the screen, make sure the sentences are updated
			if (sentenceGroup != null) {
//				sentencePublish.onNext(sentenceGroup)
			}
		}
	}

	fun sentenceObservable(): PublishSubject<SentenceGroup> {
		return sentencePublish
	}

	fun finishObservable(): PublishSubject<Day> {
		return finishPublish
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