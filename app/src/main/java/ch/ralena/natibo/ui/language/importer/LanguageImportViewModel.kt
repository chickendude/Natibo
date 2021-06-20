package ch.ralena.natibo.ui.language.importer

import ch.ralena.natibo.R
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.ui.language.list.LanguageListFragment
import ch.ralena.natibo.utils.ScreenNavigator
import javax.inject.Inject

class LanguageImportViewModel @Inject constructor(
	private val screenNavigator: ScreenNavigator
) :
	BaseViewModel<LanguageImportViewModel.Listener>() {
	interface Listener {

	}
	
	private fun loadLanguageListFragment() {
		screenNavigator.toLanguageListFragment()
	}
}