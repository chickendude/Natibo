package ch.ralena.natibo.settings.types

interface BaseSetting<T> {
	val key: String
	val nameId: Int
	val descriptionId: Int
	fun get(): T
	fun set(newValue: T)
}

