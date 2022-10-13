package ch.ralena.natibo.utils

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object Utils {
	fun alert(context: Context?, title: String?, message: String?) {
		val dialog = AlertDialog.Builder(
			context!!
		).create()
		dialog.setTitle(title)
		dialog.setMessage(message)
		dialog.show()
	}

	fun isNumeric(string: String?): Boolean {
		if (string == null || string == "") return false
		for (c in string.toCharArray()) {
			if (!Character.isDigit(c)) return false
		}
		return true
	}

	internal fun readZip(inputStream: InputStream, body: (zis: ZipInputStream) -> Unit) {
		BufferedInputStream(inputStream).use { bis ->
			ZipInputStream(bis).use { zis ->
				body(zis)
			}
		}
	}
}

fun View.show() {
	visibility = View.VISIBLE
}

fun View.hide() {
	visibility = View.GONE
}