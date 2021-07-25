package ch.ralena.natibo.ui.study.insession.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import ch.ralena.natibo.MainApplication
import ch.ralena.natibo.R
import ch.ralena.natibo.di.module.WorkerModule
import ch.ralena.natibo.ui.language.importer.ImportProgress
import ch.ralena.natibo.ui.language.importer.LanguageImportFragment
import ch.ralena.natibo.ui.language.importer.worker.listener.PackImporterListener
import kotlinx.coroutines.delay
import javax.inject.Inject


enum class Status {
	IN_PROGRESS,
	SUCCESS,
	FAILURE
}

/**
 * A worker that imports a pack into the database in the background.
 */
class StudySessionWorker(context: Context, parameters: WorkerParameters) :
	CoroutineWorker(context, parameters) {
	companion object {
		val TAG: String = StudySessionWorker::class.java.simpleName
		const val NOTIFICATION_ID = 1
		const val CHANNEL_ID = "study_session_id"
		const val BUFFER_SIZE = 1024
	}

	private val workerComponent by lazy {
		(applicationContext as MainApplication).appComponent.newWorkerComponent(WorkerModule(this))
	}

//	@Inject
//	lateinit var viewModel: PackImporterViewModel

	private var status = Status.IN_PROGRESS
	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	private lateinit var notificationBuilder: NotificationCompat.Builder

	override suspend fun doWork(): Result {
		injectDependencies()

		val uriString = inputData.getString("uri")!!
		val foregroundInfo = createForegroundInfo(uriString)
		setForeground(foregroundInfo)
//		viewModel.registerListener(this)
//		viewModel.importPack(Uri.parse(uriString))

		// Poll for status changes
		while (status == Status.IN_PROGRESS) {
			delay(500)
		}
		// Give us some time to handle post-completion cleanup before killing the worker
		delay(500)
		return when (status) {
			Status.SUCCESS -> Result.success()
			else -> Result.failure()
		}
	}

	private fun injectDependencies() {
		workerComponent.inject(this)
	}

	// region Notification Setup--------------------------------------------------------------------
	private fun createForegroundInfo(contentText: String): ForegroundInfo {
		val title = "Study Session"
		val cancel = "Cancel"
		val intent = WorkManager.getInstance(applicationContext)
			.createCancelPendingIntent(id)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			createChannel()

		notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
			.setOnlyAlertOnce(true)
			.setContentTitle(title)
			.setTicker(title)
			.setContentText(contentText)
			.setSmallIcon(R.drawable.ic_logo)
			.setSilent(true)
			.setOngoing(true)
			.addAction(android.R.drawable.ic_delete, cancel, intent)
		return ForegroundInfo(NOTIFICATION_ID, notificationBuilder.build())
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun createChannel() {
		val channelName = "Study Session"
		val channelDescription = "This channel is used for study sessions."
		val importance = NotificationManager.IMPORTANCE_DEFAULT
		val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
			description = channelDescription
		}
		notificationManager.createNotificationChannel(channel)
	}
	// endregion Notification Setup-----------------------------------------------------------------

	// region ViewModel Listener------------------------------------------------------------
	// endregion CountFilesUseCase Listener---------------------------------------------------------

	// region Helper functions----------------------------------------------------------------------
	private fun getData(type: ImportProgress) =
		Data.Builder().putInt(LanguageImportFragment.WORKER_ACTION, type.ordinal)

	private fun updateNotification(text: String) {
		notificationBuilder.setContentText(text)
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
	}
	// endregion Helper functions-------------------------------------------------------------------
}
