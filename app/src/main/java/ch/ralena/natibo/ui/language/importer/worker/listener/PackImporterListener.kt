package ch.ralena.natibo.ui.language.importer.worker.listener

import ch.ralena.natibo.ui.language.importer.worker.ImportException

interface PackImporterListener {
	/**
	 * Notification text should be updated.
	 *
	 * @param message Text to place into the notification.
	 */
	fun onNotificationUpdate(message: String)

	/**
	 * ActionText should be updated.
	 *
	 * The action text is the text in the fragment displaying what is currently being worked on.
	 *
	 * @param message Text to place into the ActionText.
	 */
	fun onActionTextUpdate(message: String)

	/**
	 * Progress should be updated.
	 *
	 * Progress goes from 0-100 and refers to the overall progress of the import process.
	 *
	 * @param progress The current progress. Should be a number between 0 and 100.
	 */
	fun onProgressUpdate(progress: Int)

	/**
	 * Notifies of an error.
	 *
	 * @param exception The error to pass in.
	 */
	fun onError(exception: ImportException)

	/**
	 * Send a warning message to show the user.
	 *
	 * This is used for things like the number of sentences and mp3s not matching. Things that
	 * the app can continue to handle but are probably not what the user intended.
	 *
	 * @param warningMsg The message to pass back to the user.
	 */
	fun onWarning(warningMsg: String)

	/**
	 * Notifies that the import process is finished.
	 */
	fun onImportComplete()
}