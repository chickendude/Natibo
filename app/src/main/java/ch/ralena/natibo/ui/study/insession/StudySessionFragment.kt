package ch.ralena.natibo.ui.study.insession

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.Day
import ch.ralena.natibo.databinding.FragmentStudySessionBinding
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.service.StudySessionServiceKt
import ch.ralena.natibo.service.StudyState
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.study.insession.views.Sentences
import ch.ralena.natibo.ui.study.overview.StudySessionOverviewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class StudySessionFragment :
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

	lateinit var course: CourseRoom

	// fields
	private val prefs: SharedPreferences? = null
	private var studySessionService: StudySessionServiceKt? = null
	private var millisLeft: Long = 0
	private var countDownTimer: CountDownTimer? = null
	private var isPaused = false

	private var serviceDisposable: Disposable? = null

	//	var sentenceDisposable: Disposable? = null
	private var finishDisposable: Disposable? = null

	override fun setupViews(view: View) {
		// load schedules from database
		val id = requireArguments().getLong(KEY_COURSE_ID)
		viewModel.fetchCourse(id)

		// connect to the studySessionService and start the session
		connectToService()

//		// if the current day is done, start the next one
//		if (course.currentDay == null || course.currentDay.isCompleted)
//			course.prepareNextDay(realm)

		binding.apply {
			// hide sentences layout until a sentence has been loaded
			sentencesLayout.visibility = View.VISIBLE

			settingsIcon.setOnClickListener {
				viewModel.settingsIconClicked()
			}

			// handle playing/pausing
			playPauseImage.setOnClickListener { v: View -> playPause(v) }
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

	private fun connectToService() {
		serviceDisposable =
			activity.sessionPublish.subscribe { service: StudySessionServiceKt ->
				studySessionService = service
				binding.sentences.setContent { Sentences(service.currentSentence()) }
//				if (course.getCurrentDay().getCurrentSentenceGroup() != null) nextSentence(
//					course.getCurrentDay().getCurrentSentenceGroup()
//				) else sessionFinished(course.getCurrentDay())
				service.currentSentence().flowWithLifecycle(lifecycle)
					.onEach { sentence ->
						if (sentence != null) loadSentence(sentence)
					}.launchIn(lifecycleScope)
//				finishDisposable = studySessionService.finishObservable()
//					.subscribe(Consumer<Day> { day: Day -> sessionFinished(day) })
				setPaused(
					service.studyState().value == StudyState.UNINITIALIZED ||
							service.studyState().value == StudyState.PAUSED
				)
				if (!isPaused) {
					startTimer()
				}
				updateTime()
				updatePlayPauseImage()
			}
	}

	private fun playPause(view: View) {
		studySessionService?.let { service ->
			if (service.studyState().value == StudyState.PLAYING) {
				service.pause()
				setPaused(true)
				countDownTimer?.cancel()
			} else {
				service.resume()
				startTimer()
			}
			updatePlayPauseImage()
		}
	}

	private fun updatePlayPauseImage() {
		if (studySessionService?.studyState()?.value == StudyState.PLAYING) {
			binding.playPauseImage.setImageResource(R.drawable.ic_pause)
		} else {
			binding.playPauseImage.setImageResource(R.drawable.ic_play)
		}
	}

	private fun sessionFinished(day: Day) {
		// mark day as completed
//		realm.executeTransaction { r: Realm? ->
//			course.addReps(course.currentDay.totalReviews)
//			day.isCompleted = true
//		}
		val fragment = StudySessionOverviewFragment()
		fragment.arguments = Bundle().apply {
			putLong(StudySessionOverviewFragment.KEY_COURSE_ID, course.id)
		}
		parentFragmentManager.beginTransaction()
			.replace(R.id.fragmentPlaceHolder, fragment)
			.commit()
	}

	private fun loadSentence(sentence: NatiboSentence) {
		updatePlayPauseImage()
		binding.sentencesLayout.visibility = View.VISIBLE

		// update number of reps remaining
//		binding.remainingRepsText.setText(
//			String.format(
//				Locale.getDefault(),
//				"%d",
//				course.getCurrentDay().getNumReviewsLeft()
//			)
//		)
//		binding.totalRepsText.setText(
//			String.format(
//				Locale.getDefault(),
//				"%d",
//				course.getTotalReps()
//			)
//		)

		// update time left
//		millisLeft = course.currentDay.timeLeft.toLong()
	}

	private fun startTimer() {
		millisLeft = millisLeft - millisLeft % 1000 - 1
		updateTime()
		// Make sure no two active timers are displayed at the same time
		countDownTimer?.cancel()
		countDownTimer = object : CountDownTimer(millisLeft, 100) {
			override fun onTick(millisUntilFinished: Long) {
				if (!isPaused) {
					millisLeft = millisUntilFinished
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

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putBoolean(KEY_IS_PAUSED, isPaused)
	}

	override fun onPause() {
		super.onPause()
		if (serviceDisposable != null) serviceDisposable!!.dispose()
//		if (sentenceDisposable != null) sentenceDisposable!!.dispose()
		if (finishDisposable != null) finishDisposable!!.dispose()
		countDownTimer?.cancel()
	}

	override fun onResume() {
		super.onResume()
		connectToService()
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
		activity.startSession(course)
		binding.courseTitleText.text = course.title
	}

	override fun onCourseNotFound(errorMsgRes: Int?) {
		if (errorMsgRes != null)
			makeToast(errorMsgRes)
	}
	// endregion ViewModel Listener-----------------------------------------------------------------

	// region Helper functions----------------------------------------------------------------------
	private fun setPaused(isPaused: Boolean) {
		this.isPaused = isPaused
	}

	private fun updateTime() {
		val secondsLeft = (millisLeft / 1000).toInt()
		binding.remainingTimeText.text = String.format(
			Locale.US,
			"%d:%02d",
			secondsLeft / 60,
			secondsLeft % 60
		)
	}
	// endregion Helper functions-------------------------------------------------------------------
}