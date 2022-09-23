package ch.ralena.natibo.service

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleService
import ch.ralena.natibo.ui.study.insession.StudySessionManager
import ch.ralena.natibo.utils.NotificationHelper
import ch.ralena.natibo.utils.STUDY_SESSION_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * TODO: Improve the pause/play functionality.
 *  Playing after the audio has completed and before the next sentence is loaded will sometimes
 *  replay the current audio file.
 */
@AndroidEntryPoint
internal class StudySessionServiceKt : LifecycleService() {
	@Inject
	lateinit var studySessionManager: StudySessionManager

	@Inject
	lateinit var notificationHelper: NotificationHelper

	private var phoneStateListener: PhoneStateListener? = null
	private lateinit var telephonyManager: TelephonyManager

	// given to clients that connect to the service
	var binder: StudyBinder = StudyBinder()

	// Broadcast Receivers
	private val becomingNoisyReceiver: BroadcastReceiver = BecomingNoisyReceiver()
	private val startSessionReceiver: BroadcastReceiver = StartSessionReceiver()

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		super.onStartCommand(intent, flags, startId)
		// check if we have attached our bundle or not
		if (intent?.action == null) {
			startForeground(STUDY_SESSION_NOTIFICATION_ID, notificationHelper.baseNotification.build())
		} else {
			handleIncomingActions(intent)
		}
		return START_STICKY
	}

	override fun onCreate() {
		super.onCreate()
		callStateListener()
		registerBecomingNoisyReceiver()
		registerStartSessionReceiver()
	}

	override fun onDestroy() {
		super.onDestroy()

		studySessionManager.stop()

		// cancel the phone state listener
		if (phoneStateListener != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
		}

		// unregister broadcast receivers
		unregisterReceiver(becomingNoisyReceiver)
		unregisterReceiver(startSessionReceiver)
	}

	override fun onBind(intent: Intent): IBinder {
		super.onBind(intent)
		return binder
	}

	// --- notification ---
	fun removeNotification() {
		// TODO: Remove
		val notificationManager: NotificationManager =
			getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(NOTIFICATION_ID)
		stopForeground(true)
	}

	private fun handleIncomingActions(playbackAction: Intent?) {
		if (playbackAction?.action == null) return

		val actionString = playbackAction.action
		when (actionString?.lowercase()) {
			ACTION_PLAY_PAUSE -> studySessionManager.togglePausePlay()
			ACTION_NEXT -> studySessionManager.nextSentence()
			ACTION_PREVIOUS -> studySessionManager.previousSentence()
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
//				isPlaying = studyState.value == StudyState.PLAYING
//				pause()
			}
			TelephonyManager.CALL_STATE_IDLE -> {}
			// back from phone call
//				if (isPlaying) resume()
		}
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
//				pause()
//				buildNotification()
			}
		}
	}

	private inner class StartSessionReceiver : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
//			if (!requestAudioFocus()) stopSelf()
		}
	}

	companion object {
		const val BROADCAST_START_SESSION = "broadcast_start_session"
		const val ACTION_PLAY_PAUSE = "action_play"
		const val ACTION_ID_PLAY_PAUSE = 0
		const val ACTION_PREVIOUS = "action_previous"
		const val ACTION_ID_PREVIOUS = 2
		const val ACTION_NEXT = "action_next"
		const val ACTION_ID_NEXT = 3
		private const val NOTIFICATION_ID = 1337
	}
}

