package ch.ralena.natibo.ui.language.importer.worker.usecase

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import ch.ralena.natibo.ui.language.importer.worker.ImportException
import java.util.*
import javax.inject.Inject

class ReadPackDataUseCase @Inject constructor(
	private val contentResolver: ContentResolver
) {
	fun extractLanguageAndPackName(uri: Uri): Pair<String, String> {
		val packFileName = extractFileName(uri)
			?: throw ImportException("URI not found: $uri")

		// Check if pack is gls file
		if (packFileName.lowercase(Locale.getDefault()).endsWith(".gls")) {
			val nameParts = packFileName.removeSuffix(".gls").split("-")
			val languageCode = nameParts.first()
			val packName = nameParts.last()
			return Pair(languageCode, packName)
		} else {
			throw ImportException("Pack not found: $packFileName")
		}
	}

	// region Helper functions----------------------------------------------------------------------
	private fun extractFileName(uri: Uri): String? = when (uri.scheme) {
		"file" -> extractFileNameFromFile(uri.toString())
		"content" -> extractFileNameFromContent(uri)
		else -> null
	}

	private fun extractFileNameFromContent(uri: Uri): String? =
		contentResolver.query(uri, null, null, null, null)?.run {
			val nameIndex = getColumnIndex(OpenableColumns.DISPLAY_NAME)
			moveToFirst()
			val fileName = getString(nameIndex)
			close()
			return fileName
		}

	private fun extractFileNameFromFile(uriString: String): String {
		val index = uriString.lastIndexOf("/")
		return uriString.substring(index + 1)
	}
	// endregion Helper functions-------------------------------------------------------------------
}
