package ch.ralena.natibo.settings.types

import androidx.annotation.StringRes
import ch.ralena.natibo.utils.StorageManager

data class BooleanSetting(
	override val key: String,
	@StringRes override val nameId: Int,
	@StringRes override val descriptionId: Int,
	private val storageManager: StorageManager,
	private val defaultValue: Boolean = false
) : BaseSetting<Boolean> {
	override fun get() = storageManager.getBoolean(key, defaultValue)
	override fun set(newValue: Boolean) = storageManager.putBoolean(key, newValue)
}

