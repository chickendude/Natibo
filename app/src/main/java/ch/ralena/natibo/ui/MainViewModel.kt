package ch.ralena.natibo.ui

import ch.ralena.natibo.service.StudyServiceManager
import ch.ralena.natibo.ui.base.BaseViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
	private val studyServiceManager: StudyServiceManager
) : BaseViewModel<MainViewModel.Listener>() {
	interface Listener

	fun stopService() {
		studyServiceManager.stopService()
	}
}