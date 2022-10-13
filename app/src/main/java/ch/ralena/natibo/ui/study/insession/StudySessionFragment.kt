package ch.ralena.natibo.ui.study.insession

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.databinding.FragmentStudySessionBinding
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.study.insession.views.PlayPause
import ch.ralena.natibo.ui.study.insession.views.Sentences
import ch.ralena.natibo.ui.study.overview.StudySessionOverviewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
internal class StudySessionFragment :
	BaseFragment<
			FragmentStudySessionBinding,
			StudySessionViewModel.Listener,
			StudySessionViewModel>(FragmentStudySessionBinding::inflate),
	StudySessionViewModel.Listener {
	companion object {
		val TAG: String = StudySessionFragment::class.java.simpleName
		const val KEY_COURSE_ID = "language_id"
		private const val KEY_IS_PAUSED = "key_is_paused"
	}

	@Inject
	lateinit var activity: MainActivity

	@Inject
	lateinit var studySessionManager: StudySessionManager

	lateinit var course: CourseRoom

	// fields
	private var millisLeft: Int = 0
	private var countDownTimer: CountDownTimer? = null

	// TODO: Look at removing
	private var isPaused = false

	override fun setupViews(view: View) {
		// load schedules from database
		val id = requireArguments().getLong(KEY_COURSE_ID)
		viewModel.fetchCourse(id)

		// connect to the studySessionService and start the session
		connectToStudySession()

		binding.apply {
			settingsIcon.setOnClickListener { viewModel.settingsIconClicked() }
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		isPaused = savedInstanceState?.getBoolean(KEY_IS_PAUSED, true) ?: true
		viewModel.registerListener(this)
		return super.onCreateView(inflater, container, savedInstanceState)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putBoolean(KEY_IS_PAUSED, isPaused)
	}

	override fun onPause() {
		super.onPause()
		countDownTimer?.cancel()
	}

	override fun onResume() {
		super.onResume()
		connectToStudySession()
	}

	override fun onDestroy() {
		super.onDestroy()
		countDownTimer?.cancel()
	}

	// region ViewModel Listener--------------------------------------------------------------------
	override fun makeToast(@StringRes stringRes: Int) {
		Toast.makeText(activity, stringRes, Toast.LENGTH_SHORT).show()
	}

	override fun onCourseLoaded(course: CourseRoom) {
		this.course = course
		studySessionManager.start(course)
		binding.courseTitleText.text = course.title
	}

	override fun onCourseNotFound(errorMsgRes: Int?) {
		if (errorMsgRes != null)
			makeToast(errorMsgRes)
	}
	// endregion ViewModel Listener-----------------------------------------------------------------

	// region Helper functions----------------------------------------------------------------------
	private fun connectToStudySession() {
		lifecycleScope.launch {
			studySessionManager.studyState().first { it != StudyState.UNINITIALIZED }
			updateTime()
			binding.sentences.setContent { Sentences(studySessionManager) }
			binding.playPauseImage.setContent { PlayPause(studySessionManager) }
			studySessionManager.studyState().first { it == StudyState.COMPLETE }
			sessionFinished()
		}
		isPaused = studySessionManager.studyState().value == StudyState.UNINITIALIZED ||
				studySessionManager.studyState().value == StudyState.PAUSED
		if (!isPaused) {
			startTimer()
		}
	}

	private fun sessionFinished() {
		studySessionManager.finishSession()
		val fragment = StudySessionOverviewFragment()
		fragment.arguments = Bundle().apply {
			putLong(StudySessionOverviewFragment.KEY_COURSE_ID, course.id)
		}
		if (!parentFragmentManager.isStateSaved)
			parentFragmentManager.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)
				.commit()
	}

	private fun startTimer() {
		millisLeft = millisLeft - millisLeft % 1000 - 1
//		updateTime()
		// Make sure no two active timers are displayed at the same time
		countDownTimer?.cancel()
		countDownTimer = object : CountDownTimer(millisLeft.toLong(), 100) {
			override fun onTick(millisUntilFinished: Long) {
				if (!isPaused) {
					millisLeft = millisUntilFinished.toInt()
					updateTime()
				}
			}

			override fun onFinish() {
				Log.d(TAG, "timer finished: $this")
			}
		}
		setPaused(false)
		countDownTimer?.start()
	}

	private fun setPaused(isPaused: Boolean) {
		this.isPaused = isPaused
	}

	private fun updateTime() {
		lifecycleScope.launch {
			studySessionManager.currentSentence().collect { sentence ->
				millisLeft = studySessionManager.session.run {
					binding.remainingRepsText.text =
						"${sentenceIndices.size - currentSentenceIndex}"
					sentenceIndices
						.subList(currentSentenceIndex, sentenceIndices.size)
						.map { index -> sentences.find { it.native.index == index } }
						.sumOf {
							it?.native?.mp3Length
								?.plus(it.target?.mp3Length ?: 0)
								?.plus(1000)
								?: 0
						}
				}
				val secondsLeft = millisLeft / 1000
				binding.remainingTimeText.text = String.format(
					Locale.US,
					"%d:%02d",
					secondsLeft / 60,
					secondsLeft % 60
				)
			}
		}
	}
// endregion Helper functions-------------------------------------------------------------------
}