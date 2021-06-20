package ch.ralena.natibo.ui.course.create.pick_language

import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.databinding.FragmentCoursePickLanguagesBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.di.module.SelectedLanguagesItemTouchHelper
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.course.create.pick_language.adapter.AvailableLanguagesAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.SelectedLanguagesAdapter
import ch.ralena.natibo.ui.base.BaseFragment
import javax.inject.Inject

/**
 * The first screen when creating a new course.
 *
 * You will be given a list of available languages and must select a base language and optionally
 * one or more target languages.
 */
class PickLanguagesFragment :
		BaseFragment<FragmentCoursePickLanguagesBinding,
				PickLanguagesViewModel.Listener,
				PickLanguagesViewModel>(FragmentCoursePickLanguagesBinding::inflate),
		AvailableLanguagesAdapter.Listener,
		PickLanguagesViewModel.Listener {

	@Inject
	lateinit var mainActivity: MainActivity

	@Inject
	lateinit var availableAdapter: AvailableLanguagesAdapter

	@Inject
	lateinit var selectedAdapter: SelectedLanguagesAdapter

	@Inject
	@SelectedLanguagesItemTouchHelper
	lateinit var itemTouchHelper: ItemTouchHelper

	// views
	private lateinit var availableRecyclerView: RecyclerView
	private lateinit var selectedRecyclerView: RecyclerView
	private lateinit var checkMenu: MenuItem


	companion object {
		val TAG: String = PickLanguagesFragment::class.java.simpleName
		const val TAG_COURSE_ID = "language_id"
	}

	override fun setupViews(view: View) {
		// enable back button and set title
		mainActivity.title = getString(R.string.select_languages)
		mainActivity.enableBackButton()
		setHasOptionsMenu(true)

		// recycler views
		availableRecyclerView = view.findViewById(R.id.availableLanguagesRecyclerView)
		availableRecyclerView.apply {
			adapter = availableAdapter
			layoutManager = GridLayoutManager(context, 3)
		}

		selectedRecyclerView = view.findViewById(R.id.selectedLanguagesRecyclerView)
		selectedRecyclerView.apply {
			adapter = selectedAdapter
			layoutManager = LinearLayoutManager(context)
		}

		// attach ItemTouchHelper
		itemTouchHelper.attachToRecyclerView(selectedRecyclerView)
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	override fun onStart() {
		super.onStart()
		availableAdapter.registerListener(this)
		viewModel.registerListener(this)
		viewModel.fetchLanguages()
	}

	override fun onStop() {
		super.onStop()
		availableAdapter.unregisterListener(this)
		viewModel.unregisterListener(this)
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.check_toolbar, menu)
		checkMenu = menu.getItem(0)
		viewModel.updateCheckMenuVisibility()
		super.onCreateOptionsMenu(menu, inflater)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == R.id.action_confirm)
			viewModel.languagesConfirmed()
		return super.onOptionsItemSelected(item)
	}

	// region ViewModel/Adapter listeners ----------------------------------------------------------
	override fun onLanguageClicked(language: LanguageRoom) {
		viewModel.addRemoveLanguage(language)
	}

	override fun onLanguageAdded(language: LanguageRoom) {
		selectedAdapter.addLanguage(language)
	}

	override fun onLanguageRemoved(language: LanguageRoom) {
		selectedAdapter.removeLanguage(language)
	}

	override fun onUpdateCheckMenuVisibility(isVisible: Boolean) {
		checkMenu.isVisible = isVisible
	}

	override fun onLanguagesLoaded(languages: List<LanguageRoom>) {
		availableAdapter.loadLanguages(languages)
	}
	// endregion ViewModel/Adapter listeners -------------------------------------------------------
}