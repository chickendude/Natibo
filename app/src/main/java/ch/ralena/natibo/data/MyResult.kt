package ch.ralena.natibo.data

sealed class MyResult {
	data class Success<T>(val data: T): MyResult()
	object Failure: MyResult()
}