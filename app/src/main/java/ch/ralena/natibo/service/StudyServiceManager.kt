package ch.ralena.natibo.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StudyServiceManager @Inject constructor(
	@ApplicationContext private val applicationContext: Context
) {
	fun startService() {
		val serviceIntent = Intent(applicationContext, StudySessionServiceKt::class.java)
		ContextCompat.startForegroundService(applicationContext, serviceIntent)
	}

	fun stopService() {
		val serviceIntent = Intent(applicationContext, StudySessionServiceKt::class.java)
		applicationContext.stopService(serviceIntent)
	}
}