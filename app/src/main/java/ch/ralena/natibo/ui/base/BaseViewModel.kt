package ch.ralena.natibo.ui.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BaseViewModel<LISTENER>: BaseListener<LISTENER>() {
	val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}