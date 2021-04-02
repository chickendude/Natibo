package ch.ralena.natibo.ui.base

abstract class BaseListener<LISTENER> {
	val listeners = HashSet<LISTENER>()

	fun registerListener(listener: LISTENER) {
		listeners.add(listener)
	}

	fun unregisterListener(listener: LISTENER) {
		listeners.remove(listener)
	}
}