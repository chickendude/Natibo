package ch.ralena.natibo.ui.course.create.pick_language

import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.databinding.FragmentCoursePickLanguagesBinding
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.create.pick_language.adapter.AvailablePacksAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.NativeLanguagesAdapter
import ch.ralena.natibo.ui.course.create.pick_language.adapter.TargetLanguagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The first screen when creating a new course.
 *
 * You will be given a list of languages to select for your native and target language. Once you
 * have selected your native and (optionally) target language, select the pack you want to study.
 */
@AndroidEntryPoint
class PickLanguagesFragment :
	BaseFragment<FragmentCoursePickLanguagesBinding,
			PickLanguagesViewModel.Listener,
			PickLanguagesViewModel>(FragmentCoursePickLanguagesBinding::inflate),
	NativeLanguagesAdapter.Listener,
	TargetLanguagesAdapter.Listener,
	AvailablePacksAdapter.Listener,
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

	override fun onStart() {
		super.onStart()
		nativeAdapter.registerListener(this)
		targetAdapter.registerListener(this)
		packsAdapter.registerListener(this)
		viewModel.registerListener(this)
		viewModel.fetchLanguages()
	}

	override fun onStop() {
		super.onStop()
		nativeAdapter.unregisterListener(this)
		targetAdapter.unregisterListener(this)
		packsAdapter.unregisterListener(this)
		viewModel.unregisterListener(this)
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

	override fun onPackSelected(pack: PackRoom) {
		viewModel.packSelected(pack)
	}
	// endregion ViewModel/Adapter listeners -------------------------------------------------------

	// region Helper functions ---------------------------------------------------------------------

	// endregion Helper functions ------------------------------------------------------------------
}