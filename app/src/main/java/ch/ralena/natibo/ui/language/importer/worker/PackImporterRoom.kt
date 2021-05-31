package ch.ralena.natibo.ui.language.importer.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import ch.ralena.natibo.MainApplication
import ch.ralena.natibo.R
import ch.ralena.natibo.data.LanguageData
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.di.module.WorkerModule
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.language.importer.ImportProgress
import ch.ralena.natibo.ui.language.importer.LanguageImportFragment
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import kotlinx.coroutines.delay
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject


enum class Status {
	IN_PROGRESS,
	SUCCESS,
	FAILURE
}

/**
 * Methods for importing a .gls file into the database.
 */
class PackImporterWorker(context: Context, parameters: WorkerParameters) :
	CoroutineWorker(context, parameters),
	PackImporterViewModel.Listener {
	companion object {
		val TAG: String = PackImporterWorker::class.java.simpleName
		const val NOTIFICATION_ID = 1
		const val CHANNEL_ID = "pack_importer_id"

		private const val STATUS_OK = 0
		private const val STATUS_MISSING_GSP = 1
		private const val STATUS_INVALID_LANGUAGE = 2
		const val BUFFER_SIZE = 1024
	}

	private val workerComponent by lazy {
		(applicationContext as MainApplication).appComponent.newWorkerComponent(WorkerModule(this))
	}

	@Inject
	lateinit var viewModel: PackImporterViewModel

	private var status = Status.IN_PROGRESS
	private val contentResolver = applicationContext.contentResolver

	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	private lateinit var notificationBuilder: NotificationCompat.Builder

	override suspend fun doWork(): Result {
		injectDependencies()

		val uriString = inputData.getString("uri")!!
		val foregroundInfo = createForegroundInfo(uriString)
		setForeground(foregroundInfo)
		viewModel.registerListener(this)
		viewModel.importPack(uriString)
		while (status == Status.IN_PROGRESS) {
			delay(500)
		}
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
//		val id = applicationContext.getString(R.string.notification_channel_id)
		val title = "Loading pack"
		val cancel = "Cancel"
		val intent = WorkManager.getInstance(applicationContext)
			.createCancelPendingIntent(getId())

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			createChannel()

		notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
			.setContentTitle(title)
			.setTicker(title)
			.setContentText(contentText)
			.setSmallIcon(R.drawable.ic_logo)
			.setOngoing(true)
			.addAction(android.R.drawable.ic_delete, cancel, intent)
		return ForegroundInfo(NOTIFICATION_ID, notificationBuilder.build())
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun createChannel() {
		val channelName = "Pack Importer"
		val channelDescription = "This channel is used for importing stuff."
		val importance = NotificationManager.IMPORTANCE_DEFAULT
		val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
			description = channelDescription
		}
		notificationManager.createNotificationChannel(channel)
	}
	// endregion Notification Setup-----------------------------------------------------------------


	// region Helper functions----------------------------------------------------------------------
	private fun updateNotification(text: String) {
		notificationBuilder.setContentText(text)
		notificationManager.notify(1, notificationBuilder.build())
	}

/*	private suspend fun loadPackFromUri(uriString: String): Boolean {
			var bos: BufferedOutputStream
			var inputStream: InputStream?
			try {
*//*
				// second pass
				var zipEntry: ZipEntry
				inputStream = getInputStream(uri)
				zis = ZipInputStream(BufferedInputStream(inputStream))

				// used to calculate length of mp3 file
				val metadataRetriever = MediaMetadataRetriever()

				// loop through files in the .gls zip
				var fileNumber = 0
				while (zis.nextEntry.also { zipEntry = it } != null) {
					val entryName = zipEntry.name
					if (entryName.contains(".mp3")) {
						val parts = entryName.split(" - ").toTypedArray()
						val language = parts[0]
						val book = parts[1]
						val number = parts[2]

						// make sure it's one of the accepted languages
						if (entryName.contains(".mp3")) {
							val folder =
								File(activity.filesDir.toString() + "/" + language + "/" + book)
							if (!folder.isDirectory) {
								folder.mkdirs()
							}

							// set up file path
							val audioFile = File(folder.absolutePath + "/" + number)

							// actually write the file
							val buffer = ByteArray(PackImporterRoom.BUFFER_SIZE)
							val fos = FileOutputStream(audioFile)
							bos = BufferedOutputStream(fos, PackImporterRoom.BUFFER_SIZE)
							var count: Int
							while (zis.read(buffer, 0, PackImporterRoom.BUFFER_SIZE)
									.also { count = it } != -1
							) {
								bos.write(buffer, 0, count)
							}

							// flush and close the stream before moving on to the next file
							bos.flush()
							bos.close()

							// now set up database objects which we will fill in after extracting all mp3s
							val index = number.replace(".mp3", "").toInt()
							val lang = realm.where(
								Language::class.java
							).equalTo("languageId", language).findFirst()

							// calculate mp3s length
							val mp3Uri = audioFile.absolutePath
							try {
								metadataRetriever.setDataSource(mp3Uri)
							} catch (re: RuntimeException) {
								// TODO: return message saying unable to read this mp3 file
								Log.d(PackImporterRoom.TAG, re.localizedMessage)
							}
							val mp3Length =
								metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
									.toInt()
							val pack: Pack = lang!!.getPack(book)
							pack.createSentenceOrUpdate(
								realm,
								index,
								null,
								null,
								null,
								mp3Uri,
								mp3Length
							)
						} else {
							Log.d(PackImporterRoom.TAG, "Skipping: $entryName")
						}
						progressSubject.onNext(++fileNumber)
					}
				}
				inputStream!!.close()
				zis.close()*//*
			} catch (e: IOException) {
				e.printStackTrace()
			}
		} else {
//			activity.runOnUiThread {
//				Toast.makeText(
//					activity.applicationContext,
//					"Sorry, this filetype is not supported!",
//					Toast.LENGTH_SHORT
//				).show()
//			}
//			actionSubject.onNext(LanguageImportFragment.ACTION_EXIT)
		}

		return true
	}*/

	/**
	 * Counts the number of files in a pack and does some basic verification to ensure files are
	 * in order.
	 */
	private suspend fun countFiles(zis: ZipInputStream): Int {
//		if (!hasGspFile) return STATUS_MISSING_GSP
//		if (LanguageData.getLanguageById(baseLanguage) == null) return STATUS_INVALID_LANGUAGE

//
//
//

		return STATUS_OK
	}

	// endregion Helper functions-------------------------------------------------------------------

	// region ViewModel Listener------------------------------------------------------------
	private fun getData(type: ImportProgress) =
		Data.Builder().putInt(LanguageImportFragment.WORKER_ACTION, type.ordinal)

	override fun onNotificationUpdate(message: String) {
		updateNotification(message)
		setProgressAsync(
			getData(ImportProgress.ACTION_TEXT)
				.putString(LanguageImportFragment.WORKER_VALUE, message)
				.build()
		)
	}

	override fun onError(exception: ImportException) {
		Toast.makeText(applicationContext, "${exception.message}", Toast.LENGTH_SHORT).show()
		status = Status.FAILURE
	}
	// endregion CountFilesUseCase Listener---------------------------------------------------------

}

class PackImporterRoom {
	private lateinit var contentResolver: ContentResolver

	private val progressSubject: PublishSubject<Int>
	private val totalSubject: PublishSubject<Int>
	private val actionSubject: PublishSubject<Int>
	private val fileNameSubject: PublishSubject<String>

	fun progressObservable(): PublishSubject<Int> {
		return progressSubject
	}

	fun totalObservable(): PublishSubject<Int> {
		return totalSubject
	}

	fun actionSubject(): PublishSubject<Int> {
		return actionSubject
	}

	fun fileNameSubject(): PublishSubject<String> {
		return fileNameSubject
	}

	fun importPack(activity: MainActivity, uri: Uri) {
		// TODO: 18/03/18 1. Do some file verification to make sure all files are indeed there
		// TODO: 18/03/18 2. Verify file names/strip the 'EN - ' bit out
		actionSubject.onNext(LanguageImportFragment.ACTION_OPENING_FILE)
		val thread = Thread {
			val realm = Realm.getDefaultInstance()
			contentResolver = activity.contentResolver
			var packFileName = ""
			if (uri.scheme == "file") {
				val index = uri.toString().lastIndexOf("/")
				packFileName = uri.toString().substring(index + 1)
			} else if (uri.scheme == "content") {
				val cursor = contentResolver.query(uri, null, null, null, null)
				if (cursor != null) {
					val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
					cursor.moveToFirst()
					packFileName = cursor.getString(nameIndex)
					cursor.close()
				} else {
					activity.runOnUiThread {
						Toast.makeText(
							activity.applicationContext,
							String.format("Error opening file: %s", uri),
							Toast.LENGTH_SHORT
						).show()
					}
					actionSubject.onNext(LanguageImportFragment.ACTION_EXIT)
				}
			}

			// pass the filename back to the fragment
			fileNameSubject.onNext(packFileName)
			if (packFileName.toLowerCase().endsWith(".gls")) {
				var bos: BufferedOutputStream
				var `is`: InputStream?
				try {
					// first pass
					`is` = getInputStream(uri)
					var zis = ZipInputStream(BufferedInputStream(`is`))

					// check if there's anything missing in the file
					val status = countFiles(zis, realm)
					if (status > STATUS_OK) {
						val stringResId: Int
						stringResId = when (status) {
							STATUS_INVALID_LANGUAGE -> R.string.language_not_supported
							STATUS_MISSING_GSP -> R.string.missing_gsp
							else -> R.string.error_opening_file
						}
						activity.runOnUiThread {
							Toast.makeText(
								activity.applicationContext,
								activity.getString(stringResId),
								Toast.LENGTH_SHORT
							).show()
						}
						actionSubject.onNext(LanguageImportFragment.ACTION_EXIT)
					}

					// second pass
					var zipEntry: ZipEntry
					`is` = getInputStream(uri)
					zis = ZipInputStream(BufferedInputStream(`is`))

					// used to calculate length of mp3 file
					val metadataRetriever = MediaMetadataRetriever()

					// loop through files in the .gls zip
					var fileNumber = 0
					while (zis.nextEntry.also { zipEntry = it } != null) {
						val entryName = zipEntry.name
						if (entryName.contains(".mp3")) {
							val parts = entryName.split(" - ").toTypedArray()
							val language = parts[0]
							val book = parts[1]
							val number = parts[2]

							// make sure it's one of the accepted languages
							if (entryName.contains(".mp3")) {
								val folder =
									File(activity.filesDir.toString() + "/" + language + "/" + book)
								if (!folder.isDirectory) {
									folder.mkdirs()
								}

								// set up file path
								val audioFile = File(folder.absolutePath + "/" + number)

								// actually write the file
								val buffer = ByteArray(BUFFER_SIZE)
								val fos = FileOutputStream(audioFile)
								bos = BufferedOutputStream(fos, BUFFER_SIZE)
								var count: Int
								while (zis.read(buffer, 0, BUFFER_SIZE).also { count = it } != -1) {
									bos.write(buffer, 0, count)
								}

								// flush and close the stream before moving on to the next file
								bos.flush()
								bos.close()

								// now set up database objects which we will fill in after extracting all mp3s
								val index = number.replace(".mp3", "").toInt()
								val lang = realm.where(
									Language::class.java
								).equalTo("languageId", language).findFirst()

								// calculate mp3s length
								val mp3Uri = audioFile.absolutePath
								try {
									metadataRetriever.setDataSource(mp3Uri)
								} catch (re: RuntimeException) {
									// TODO: return message saying unable to read this mp3 file
									Log.d(TAG, re.localizedMessage)
								}
								val mp3Length =
									metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
										.toInt()
								val pack: Pack = lang!!.getPack(book)
								pack.createSentenceOrUpdate(
									realm,
									index,
									null,
									null,
									null,
									mp3Uri,
									mp3Length
								)
							} else {
								Log.d(TAG, "Skipping: $entryName")
							}
							progressSubject.onNext(++fileNumber)
						}
					}
					`is`!!.close()
					zis.close()
				} catch (e: IOException) {
					e.printStackTrace()
				}
			} else {
				activity.runOnUiThread {
					Toast.makeText(
						activity.applicationContext,
						"Sorry, this filetype is not supported!",
						Toast.LENGTH_SHORT
					).show()
				}
				actionSubject.onNext(LanguageImportFragment.ACTION_EXIT)
			}
		}
		thread.start()
	}

	@Throws(FileNotFoundException::class)
	private fun getInputStream(uri: Uri): InputStream? {
		return if (uri.scheme == "file") {
			val file = File(uri.path)
			FileInputStream(file)
		} else contentResolver.openInputStream(uri)
	}

	/**
	 * Counts the number of files in a pack and does some basic verification to ensure files are
	 * in order.
	 */
	@Throws(IOException::class)
	private fun countFiles(zis: ZipInputStream, realm: Realm): Int {
		// update action in fragment
		actionSubject.onNext(LanguageImportFragment.ACTION_COUNTING_SENTENCES)
		val buffer = ByteArray(BUFFER_SIZE)
		var zipEntry: ZipEntry
		var numFiles = 0
		var bytesRead: Int
		val baos = ByteArrayOutputStream()

		// calculate number of files
		var baseLanguage = ""
		var targetLanguage = ""
		var packName = ""
		var hasGspFile = false
		while (zis.nextEntry.also { zipEntry = it } != null) {
			// only count the sentence mp3 files
			if (zipEntry.name.endsWith("mp3")) {
				packName = zipEntry.name.split(" - ").toTypedArray()[1]
				numFiles++
				actionSubject.onNext(LanguageImportFragment.ACTION_COUNTING_SENTENCES)
				totalSubject.onNext(numFiles)
			} else if (zipEntry.name.endsWith(".gsp")) {
				hasGspFile = true
				actionSubject.onNext(LanguageImportFragment.ACTION_READING_SENTENCES)
				// extract base language and target language from file name
				val nameParts = zipEntry.name.split("-").toTypedArray()
				baseLanguage = nameParts[0].trim { it <= ' ' }
				if (nameParts.size > 3) targetLanguage = nameParts[1].trim { it <= ' ' }
				// extract contents of file into the StringBuilder
				while (zis.read(buffer, 0, BUFFER_SIZE).also { bytesRead = it } >= 0) {
					baos.write(buffer, 0, bytesRead)
				}
			}
		}
		if (!hasGspFile) return STATUS_MISSING_GSP
		if (LanguageData.getLanguageById(baseLanguage) == null) return STATUS_INVALID_LANGUAGE

		// --- begin transaction
		realm.beginTransaction()

		// create base language and pack if they don't exist
		var base = realm.where(
			Language::class.java
		).equalTo("languageId", baseLanguage).findFirst()
		if (base == null) {
			base = realm.createObject(Language::class.java, baseLanguage)
		}
		var basePack: Pack? = base!!.getPack(packName)
		if (basePack == null) {
			basePack = realm.createObject<Pack>(Pack::class.java, UUID.randomUUID().toString())
			basePack.setBook(packName)
			base.packs.add(basePack)
		}

		// create target language and pack if they don't exist
		var target: Language?
		var targetPack: Pack? = null
		if (targetLanguage != "") {
			target =
				realm.where(Language::class.java).equalTo("languageId", targetLanguage).findFirst()
			if (target == null) {
				target = realm.createObject(Language::class.java, targetLanguage)
			}
			targetPack = target!!.getPack(packName)
			if (targetPack == null) {
				targetPack =
					realm.createObject<Pack>(Pack::class.java, UUID.randomUUID().toString())
				targetPack.setBook(packName)
				target.packs.add(targetPack)
			}
		}
		realm.commitTransaction()
		// --- end transaction


		// update action in fragment
		actionSubject.onNext(LanguageImportFragment.ACTION_EXTRACTING_TEXT)
		progressSubject.onNext(0)
		totalSubject.onNext(numFiles / if (targetLanguage == "") 1 else 2)
		val sentenceList = baos.toString("UTF-8").split("\n").toTypedArray()
		val sections = sentenceList[0].split("\t").toTypedArray()
		for (i in 1 until sentenceList.size) {
			progressSubject.onNext(i)
			val sentenceParts = sentenceList[i].split("\t").toTypedArray()
			val index = sentenceParts[0].toInt()
			var sentence: String? = null
			var translation: String? = null
			var ipa: String? = null
			var romanization: String? = null
			for (j in sentenceParts.indices) {
				val value = sentenceParts[j]
				when (sections[j]) {
					"index" -> {
					}
					"sentence" -> sentence = value
					"translation" -> translation = value
					"IPA" -> ipa = value
					"romanization" -> romanization = value
				}
			}

			// create or update target and base sentences
			if (targetLanguage != "") targetPack?.createSentenceOrUpdate(
				realm,
				index,
				translation,
				ipa,
				romanization,
				null,
				0
			)
			basePack?.createSentenceOrUpdate(realm, index, sentence, ipa, romanization, null, 0)
		}
		actionSubject.onNext(LanguageImportFragment.ACTION_EXTRACTING_AUDIO)
		progressSubject.onNext(0)
		totalSubject.onNext(numFiles)
		return STATUS_OK
	}

	companion object {
		val TAG = PackImporterRoom::class.java.simpleName
		private const val STATUS_OK = 0
		private const val STATUS_MISSING_GSP = 1
		private const val STATUS_INVALID_LANGUAGE = 2
		private const val BUFFER_SIZE = 1024
	}

	init {
		progressSubject = PublishSubject.create()
		totalSubject = PublishSubject.create()
		actionSubject = PublishSubject.create()
		fileNameSubject = PublishSubject.create()
	}
}