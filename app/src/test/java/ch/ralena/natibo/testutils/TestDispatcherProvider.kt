package ch.ralena.natibo.testutils

import ch.ralena.natibo.utils.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class TestDispatcherProvider(private val testCoroutineDispatcher: TestCoroutineDispatcher) : DispatcherProvider {
	override fun main() = testCoroutineDispatcher
	override fun io() = testCoroutineDispatcher
	override fun default() = testCoroutineDispatcher
	override fun unconfined() = testCoroutineDispatcher
}