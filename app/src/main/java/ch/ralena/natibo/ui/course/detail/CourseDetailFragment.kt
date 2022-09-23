package ch.ralena.natibo.ui.course.detail

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.databinding.FragmentCourseDetailBinding
import ch.ralena.natibo.service.StudyServiceManager
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CourseDetailFragment
	: BaseFragment<
		FragmentCourseDetailBinding,
		CourseDetailViewModel.Listener,
		CourseDetailViewModel>(FragmentCourseDetailBinding::inflate),
	CourseDetailViewModel.Listener {

	companion object {
		val TAG: String = CourseDetailFragment::class.java.simpleName
		const val TAG_COURSE_ID = "course_id"
	}

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var studyServiceManager: StudyServiceManager

	override fun setupViews(view: View) {
		activity.enableBackButton()

		viewModel.registerListener(this)

		binding.apply {
			startSessionButton.setOnClickListener {
				viewModel.startSession()
			}

			deleteIcon.setOnClickListener {
				Snackbar.make(view, R.string.confirm_delete, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.delete) {
						viewModel.deleteCourse()
						studyServiceManager.stopService()
					}.show()
			}

			settingsIcon.setOnClickListener {
				viewModel.openSettings()
			}
		}

		requireArguments().getLong(TAG_COURSE_ID).let {
			viewModel.fetchData(it)
		}
	}

	// region ViewModel listeners ------------------------------------------------------------------
	override fun onCourseFetched(course: CourseRoom) {
		activity.title = course.title
		loadCourseInfo(course)
	}

	override fun onPackFetched(pack: PackRoom) {
		binding.packNameText.text = pack.name
	}

	override fun onPackNotFound() {
		Toast.makeText(context, getString(R.string.pack_not_found), Toast.LENGTH_LONG).show()
	}

	override fun onCourseNotFound() {
		Toast.makeText(context, R.string.course_not_found, Toast.LENGTH_SHORT).show()
	}

	override fun updateSessionStatusText(@StringRes msgId: Int) {
		binding.startSessionButton.text = getString(msgId)
	}

	override fun onLanguageFetched(language: LanguageRoom) {
		binding.flagImage.setImageResource(language.flagDrawable)
	}
	// endregion ViewModel listeners ---------------------------------------------------------------

	// region Helper functions----------------------------------------------------------------------
	private fun loadCourseInfo(course: CourseRoom) {
		binding.totalRepsText.text = String.format(Locale.US, "%d", course.repCount)
		// TODO: Restore
//		binding.totalSentencesSeenText.text =
//			String.format(Locale.US, "%d", course.numSentencesSeen)
		// Use native language if there is no target language.
		viewModel.fetchLanguage(course.targetLanguageId ?: course.nativeLanguageId)
	}
	// endregion Helper functions-------------------------------------------------------------------
}