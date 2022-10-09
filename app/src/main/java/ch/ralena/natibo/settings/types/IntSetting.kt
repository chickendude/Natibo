package ch.ralena.natibo.settings.types

import androidx.annotation.StringRes
import ch.ralena.natibo.utils.StorageManager

data class IntSetting(
	override val key: String,
	@StringRes override val nameId: Int,
	@StringRes override val descriptionId: Int,
	private val storageManager: StorageManager,
	private val defaultValue: Int = 0
) : BaseSetting<Int> {
	override fun get() = storageManager.getInt(key, defaultValue)
	override fun set(newValue: Int) = storageManager.putInt(key, newValue)
}

