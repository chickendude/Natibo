package ch.ralena.natibo.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREFERENCES = "ch.ralena.natibo.PREFERENCES"

class StorageManager @Inject constructor(@ApplicationContext private val context: Context) {
	private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

	fun putBoolean(key: String, newValue: Boolean) = edit { putBoolean(key, newValue) }
	fun getBoolean(key: String, defaultValue: Boolean) = preferences.getBoolean(key, defaultValue)

	fun putInt(key: String, newValue: Int) = edit { putInt(key, newValue) }
	fun getInt(key: String, defaultValue: Int): Int = preferences.getInt(key, defaultValue)

	// region Helper functions ---------------------------------------------------------------------
	private fun edit(block: SharedPreferences.Editor.() -> Unit) {
		preferences.edit().apply { block(this) }.apply()
	}
	// endregion Helper functions ------------------------------------------------------------------

}

