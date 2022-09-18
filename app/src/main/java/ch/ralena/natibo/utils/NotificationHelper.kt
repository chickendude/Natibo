package ch.ralena.natibo.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ch.ralena.natibo.R
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.service.StudySessionServiceKt
import ch.ralena.natibo.ui.study.insession.StudyState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val STUDY_SESSION_CHANNEL_ID = "study_session_channel"
const val STUDY_SESSION_NOTIFICATION_ID = 1337

@Singleton
internal class NotificationHelper @Inject constructor(
	@ApplicationContext private val applicationContext: Context
) {
	private val notificationManager = NotificationManagerCompat.from(applicationContext)

	init {
		createChannel()
	}

	val baseNotification
		get() = NotificationCompat.Builder(applicationContext, STUDY_SESSION_CHANNEL_ID)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setShowWhen(false)
			.setOnlyAlertOnce(true)
			.setSmallIcon(R.drawable.ic_logo)

	fun updateStudySessionNotification(
		studyState: StudyState,
		mediaSession: MediaSessionCompat,
		sentence: NatiboSentence,
	) {
		mediaSession.setMetadata(
			MediaMetadataCompat.Builder()
				.putString(
					MediaMetadata.METADATA_KEY_TITLE,
					sentence.native.original
				)
				.putString(
					MediaMetadata.METADATA_KEY_ALBUM,
					sentence.target?.original
				)
				.build()
		)
		val playPauseDrawable = if (studyState == StudyState.PAUSED) {
			android.R.drawable.ic_media_play
		} else {
			android.R.drawable.ic_media_pause
		}
		val notification = baseNotification
			.setOngoing(studyState == StudyState.PLAYING)
			.setStyle(
				androidx.media.app.NotificationCompat.MediaStyle()
					.setMediaSession(mediaSession.sessionToken)
					.setShowActionsInCompactView(0, 1, 2)
			)
			.setContentText(sentence.native.original)
			.setContentTitle(sentence.target?.original)
			.addAction(
				android.R.drawable.ic_media_previous,
				"prev sentence",
				iconAction(StudySessionServiceKt.ACTION_ID_PREVIOUS)
			)
			.addAction(
				playPauseDrawable,
				"pause",
				iconAction(StudySessionServiceKt.ACTION_ID_PLAY_PAUSE)
			)
			.addAction(
				android.R.drawable.ic_media_next,
				"next sentence",
				iconAction(StudySessionServiceKt.ACTION_ID_NEXT)
			).build()
		notificationManager.notify(STUDY_SESSION_NOTIFICATION_ID, notification)
	}

	fun removeNotification() {
		notificationManager.cancel(STUDY_SESSION_NOTIFICATION_ID)
		val intent = Intent(applicationContext, StudySessionServiceKt::class.java)
		applicationContext.stopService(intent)
	}

	// region Helper functions ---------------------------------------------------------------------
	private fun createChannel() {
		val channel = NotificationChannelCompat.Builder(
			STUDY_SESSION_CHANNEL_ID,
			NotificationManagerCompat.IMPORTANCE_HIGH
		)
			.setName("Study Session")
			.setDescription("Displays your sentences during a study session")
			.setSound(null, null)
			.build()
		notificationManager.createNotificationChannel(channel)
	}

	private fun iconAction(actionId: Int): PendingIntent? {
		val iconIntent = Intent(applicationContext, StudySessionServiceKt::class.java)
		when (actionId) {
			StudySessionServiceKt.ACTION_ID_PLAY_PAUSE ->
				iconIntent.action = StudySessionServiceKt.ACTION_PLAY_PAUSE
			StudySessionServiceKt.ACTION_ID_NEXT ->
				iconIntent.action = StudySessionServiceKt.ACTION_NEXT
			StudySessionServiceKt.ACTION_ID_PREVIOUS ->
				iconIntent.action = StudySessionServiceKt.ACTION_PREVIOUS
			else -> return null
		}
		return PendingIntent.getService(applicationContext, actionId, iconIntent, 0)
	}
	// endregion Helper functions ------------------------------------------------------------------
}