package ch.ralena.natibo.ui.language.list

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.LanguageWithPacks
import ch.ralena.natibo.databinding.FragmentLanguageListBinding
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.language.list.adapter.LanguageListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageListFragment :
	BaseFragment<FragmentLanguageListBinding,
			LanguageListViewModel.Listener,
			LanguageListViewModel>(FragmentLanguageListBinding::inflate),
	LanguageListAdapter.Listener,
	LanguageListViewModel.Listener {

	companion object {
		val TAG: String = LanguageListFragment::class.java.simpleName
	}

	@Inject
	lateinit var languageAdapter: LanguageListAdapter

	@Inject
	lateinit var mainActivity: MainActivity

	override fun setupViews(view: View) {
		viewModel.registerListener(this)
		mainActivity.title = getString(R.string.languages)

		viewModel.fetchLanguages()

		// set up FAB
		binding.fab.setOnClickListener {
			mainActivity.importLanguagePack()
		}
	}

	override fun onStart() {
		super.onStart()
		viewModel.registerListener(this)
		languageAdapter.registerListener(this)
	}

	override fun onStop() {
		super.onStop()
		viewModel.unregisterListener(this)
		languageAdapter.unregisterListener(this)
	}

	override fun onLanguageClicked(language: LanguageRoom) {
		viewModel.languageSelected(language)
	}

	// region ViewModel listeners-------------------------------------------------------------------
	override fun onLanguagesLoaded(
		languagesWithPacks: List<LanguageWithPacks>,
		sentenceCounts: List<Int>
	) {
		languageAdapter.loadLanguages(languagesWithPacks, sentenceCounts)
		updateRecyclerView()
	}
	// endregion ViewModel listeners----------------------------------------------------------------

	// region Helper functions----------------------------------------------------------------------
	private fun updateRecyclerView() {
		binding.recyclerView.apply {
			visibility = viewModel.getRecyclerViewVisibility()
			adapter = languageAdapter
			layoutManager = GridLayoutManager(context, 2)
		}

		binding.noCoursesText.visibility = viewModel.getNoCourseTextVisibility()
	}
	// endregion Helper functions-------------------------------------------------------------------
}