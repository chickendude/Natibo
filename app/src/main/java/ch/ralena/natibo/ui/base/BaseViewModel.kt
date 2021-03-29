package ch.ralena.natibo.ui.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BaseViewModel<LISTENER> {
	val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

	val listeners = HashSet<LISTENER>()

	fun registerListener(listener: LISTENER) {
		listeners.add(listener)
	}

	fun unregisterListener(listener: LISTENER) {
		listeners.remove(listener)
	}
}