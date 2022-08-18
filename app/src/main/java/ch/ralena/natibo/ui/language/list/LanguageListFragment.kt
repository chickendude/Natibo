package ch.ralena.natibo.ui.language.list

import android.view.LayoutInflater
import android.view.View
import ch.ralena.natibo.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ch.ralena.natibo.ui.language.list.adapter.LanguageListAdapter
import androidx.recyclerview.widget.GridLayoutManager
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.LanguageWithPacks
import ch.ralena.natibo.databinding.FragmentLanguageListBinding
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList
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

	lateinit var languages: ArrayList<Language>

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
	override fun onLanguagesLoaded(languagesWithPacks: List<LanguageWithPacks>) {
		languageAdapter.loadLanguages(languagesWithPacks)
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