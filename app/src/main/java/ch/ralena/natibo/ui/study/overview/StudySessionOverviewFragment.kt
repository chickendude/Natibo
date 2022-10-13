package ch.ralena.natibo.ui.study.overview

import android.view.View
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.databinding.FragmentStudySessionOverviewBinding
import ch.ralena.natibo.model.NatiboSession
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.shared_components.SentenceList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudySessionOverviewFragment :
	BaseFragment<
			FragmentStudySessionOverviewBinding,
			StudySessionOverviewViewModel.Listener,
			StudySessionOverviewViewModel>(FragmentStudySessionOverviewBinding::inflate),
	StudySessionOverviewViewModel.Listener {
	var course: CourseRoom? = null
	private lateinit var activity: MainActivity

	override fun setupViews(view: View) {
		activity = requireActivity() as MainActivity
		activity.title = getString(R.string.session_overview)
		// load schedules from database
		viewModel.registerListener(this)
		val id = requireArguments().getLong(KEY_COURSE_ID)
		viewModel.getSentences(id)
	}

	override fun sessionLoaded(
		session: NatiboSession,
		nativeLanguage: LanguageRoom?,
		targetLanguage: LanguageRoom?
	) {
		binding.composeView.setContent {
			SentenceList(
				sentences = session.sentences,
				nativeLanguage = nativeLanguage,
				targetLanguage = targetLanguage
			)
		}
	}

	companion object {
		private val TAG = StudySessionOverviewFragment::class.java.simpleName
		const val KEY_COURSE_ID = "language_id"
	}
}
