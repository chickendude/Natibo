package ch.ralena.natibo.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

interface DispatcherProvider {
	fun main(): CoroutineDispatcher = Dispatchers.Main
	fun default(): CoroutineDispatcher = Dispatchers.Default
	fun io(): CoroutineDispatcher = Dispatchers.IO
	fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}

internal class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider