package ch.ralena.natibo.utils

import android.content.Context
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

	class Storage(private val context: Context) {
		fun putDayId(dayId: String?) {
			val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
			preferences.edit().putString(KEY_DAY_ID, dayId).apply()
		}

		val dayId: String?
			get() {
				val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
				return preferences.getString(KEY_DAY_ID, "")
			}

		fun putCourseId(id: Long) {
		}

		var courseId: Long
			get() {
				val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
				return preferences.getLong(KEY_COURSE_ID, 0L)
			}
			set(value) {
				val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
				preferences.edit().putLong(KEY_COURSE_ID, value).apply()
			}

		companion object {
			private const val PREFERENCES = "ch.ralena.natibo.PREFERENCES"
			private const val KEY_DAY_ID = "key_day_id"
			private const val KEY_COURSE_ID = "key_course_id"
		}
	}

	internal fun readZip(inputStream: InputStream, body: (zis: ZipInputStream) -> Unit) {
		BufferedInputStream(inputStream).use { bis ->
			ZipInputStream(bis).use { zis ->
				body(zis)
			}
		}
	}
}