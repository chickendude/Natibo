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
import ch.ralena.natibo.databinding.FragmentLanguageListBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import java.util.ArrayList
import javax.inject.Inject

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
		mainActivity.title = getString(R.string.languages)

		viewModel.fetchLanguages()

		view.findViewById<RecyclerView>(R.id.recyclerView).apply {
			visibility = viewModel.getRecyclerViewVisibility()
			adapter = languageAdapter
			layoutManager = GridLayoutManager(context, 2)
		}

		view.findViewById<TextView>(R.id.noCoursesText).apply {
			visibility = viewModel.getNoCourseTextVisibility()
		}

		// set up FAB
		view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
			(activity as MainActivity?)!!.importLanguagePack()
		}
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
		viewModel.registerListener(this)
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

	override fun onLanguageClicked(language: Language) {
		viewModel.languageSelected(language)
	}

	override fun onLanguagesLoaded(languages: List<Language>) {
		languageAdapter.loadLanguages(languages)
	}
}