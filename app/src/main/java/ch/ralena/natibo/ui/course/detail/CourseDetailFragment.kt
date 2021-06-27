package ch.ralena.natibo.ui.course.detail

import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.LanguageData
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.databinding.FragmentCourseDetailBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.adapter.PackAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

// TODO: 13/04/18 if no sentence sets have been chosen, prompt to select sentence packs.
class CourseDetailFragment
	: BaseFragment<
		FragmentCourseDetailBinding,
		CourseDetailViewModel.Listener,
		CourseDetailViewModel>(FragmentCourseDetailBinding::inflate),
	PackAdapter.Listener,
	CourseDetailViewModel.Listener {

	companion object {
		val TAG: String = CourseDetailFragment::class.java.simpleName
		const val TAG_COURSE_ID = "language_id"
	}

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var packAdapter: PackAdapter

	private val singleThreadContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

	override fun setupViews(view: View) {
		activity.enableBackButton()

		packAdapter.registerListener(this)
		viewModel.registerListener(this)

		binding.apply {
			booksRecyclerView.apply {
				adapter = packAdapter
				layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
			}

			startSessionButton.setOnClickListener {
				viewModel.startSession()
			}

			deleteIcon.setOnClickListener {
				Snackbar.make(view, R.string.confirm_delete, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.delete) {
						viewModel.deleteCourse()
						activity.stopSession()
					}.show()
			}

			settingsIcon.setOnClickListener {
				viewModel.openSettings()
			}
		}

		requireArguments().getLong(TAG_COURSE_ID).let {
			viewModel.fetchCourse(it)
		}
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	override fun onBookClicked(pack: Pack) {
		val packId = pack.id
		// todo clean up with Room
		// We have to use our own single thread context, otherwise Realm complains about access on
		// other threads. Can't wait to switch over to Room...
		lifecycleScope.launch(Dispatchers.Main) {
			withContext(singleThreadContext) {
				viewModel.addRemovePack(packId)
			}
			packAdapter.toggleTargetPack(pack)
		}
	}

	override fun onCourseFetched(course: CourseRoom) {
		activity.title = course.title
//		val targetLanguage = getLanguage(course.targetLanguageId)
		loadCourseInfo(course)
//		val matchingPacks: List<Pack> =
//			targetLanguage.getMatchingPacks(course.languages.last())
//		packAdapter.loadPacks(matchingPacks)
	}

	override fun onCourseNotFound() {
		Toast.makeText(context, R.string.course_not_found, Toast.LENGTH_SHORT).show()
	}

	override fun noPacksSelected() {
		Toast.makeText(context, R.string.add_book_first, Toast.LENGTH_SHORT).show()
	}

	override fun onSessionStarted() {
		binding.startSessionButton.text = getString(R.string.continue_session)
	}

	override fun onSessionNotStarted() {
		binding.startSessionButton.text = getString(R.string.start_session)
	}

	override fun onLanguageFetched(language: LanguageRoom) {
		binding.flagImageView.setImageResource(language.flagDrawable)
	}

	// region Helper functions----------------------------------------------------------------------
	private fun getLanguage(code: String) = LanguageData.getLanguageById(code)

	private fun loadCourseInfo(course: CourseRoom) {
		binding.totalRepsText.text = String.format(Locale.US, "%d", course.repCount)
//		binding.totalSentencesSeenText.text =
//			String.format(Locale.US, "%d", course.numSentencesSeen)
		viewModel.fetchLanguage(course.targetLanguageId)
	}
	// endregion Helper functions-------------------------------------------------------------------
}