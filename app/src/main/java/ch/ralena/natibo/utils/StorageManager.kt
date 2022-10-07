package ch.ralena.natibo.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREFERENCES = "ch.ralena.natibo.PREFERENCES"

class StorageManager @Inject constructor(@ApplicationContext private val context: Context) {
	private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

	fun putBoolean(key: String, value: Boolean) {
		preferences.edit().putBoolean(key, value).apply()
	}

	fun getBoolean(key: String, defaultValue: Boolean) = preferences.getBoolean(key, defaultValue)
}

