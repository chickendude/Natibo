package ch.ralena.natibo.ui.course.create.pick_language

import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.databinding.FragmentCoursePickLanguagesBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.course.create.pick_language.adapter.NativeLanguagesAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.AvailablePacksAdapter
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.create.pick_language.adapter.TargetLanguagesAdapter
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
	NativeLanguagesAdapter.Listener,
	TargetLanguagesAdapter.Listener,
	PickLanguagesViewModel.Listener {

	@Inject
	lateinit var mainActivity: MainActivity

	@Inject
	lateinit var nativeAdapter: NativeLanguagesAdapter

	@Inject
	lateinit var packsAdapter: AvailablePacksAdapter

	@Inject
	lateinit var targetAdapter: TargetLanguagesAdapter

	// views
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

		binding.apply {
			// Set up RecyclerViews
			nativeLanguagesRecyclerView.apply {
				adapter = nativeAdapter
				layoutManager = GridLayoutManager(context, 3)
			}

			targetLanguagesRecyclerView.apply {
				adapter = targetAdapter
				layoutManager = GridLayoutManager(context, 3)
			}

			availablePacksRecyclerView.apply {
				adapter = packsAdapter
				layoutManager = GridLayoutManager(context, 3)
			}
		}
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	override fun onStart() {
		super.onStart()
		nativeAdapter.registerListener(this)
		targetAdapter.registerListener(this)
		viewModel.registerListener(this)
		viewModel.fetchLanguages()
	}

	override fun onStop() {
		super.onStop()
		nativeAdapter.unregisterListener(this)
		targetAdapter.unregisterListener(this)
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
	override fun onNativeLanguageClicked(language: LanguageRoom) {
		viewModel.changeNativeLanguage(language)
	}

	override fun onNativeLanguageChanged(language: LanguageRoom?) {
		nativeAdapter.setSelectedLanguage(language)
	}

	override fun onTargetLanguageClicked(language: LanguageRoom) {
		viewModel.changeTargetLanguage(language)
	}

	override fun onTargetLanguageChanged(language: LanguageRoom?) {
		targetAdapter.setSelectedLanguage(language)
	}

	override fun onUpdateCheckMenuVisibility(isVisible: Boolean) {
		checkMenu.isVisible = isVisible
	}

	override fun onLanguagesLoaded(languages: List<LanguageRoom>) {
		nativeAdapter.loadLanguagesWithPacks(languages)
	}

	override fun onTargetLanguagesChanged(languages: List<LanguageRoom>) {
		binding.selectLanguageLabel.visibility =
			if (languages.isEmpty()) View.VISIBLE else View.GONE
		targetAdapter.loadLanguagesWithPacks(languages)
	}

	override fun onPacksUpdated(packs: List<PackRoom>) {
		packsAdapter.loadPacks(packs)
	}
	// endregion ViewModel/Adapter listeners -------------------------------------------------------

	// region Helper functions ---------------------------------------------------------------------

	// endregion Helper functions ------------------------------------------------------------------
}