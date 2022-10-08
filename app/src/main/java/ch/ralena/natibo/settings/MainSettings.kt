package ch.ralena.natibo.settings

import ch.ralena.natibo.R
import ch.ralena.natibo.settings.types.BooleanSetting
import ch.ralena.natibo.ui.fragment.BooleanSetting
import ch.ralena.natibo.utils.StorageManager
import javax.inject.Inject

class MainSettings @Inject constructor(storageManager: StorageManager) {
	val pauseOnOpen: BooleanSetting = BooleanSetting(
		"main_pause_on_open",
		R.string.settings_pause_on_open_title,
		R.string.settings_pause_on_open_description,
		storageManager
	)
}

