package ch.ralena.natibo.ui.course.list

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.databinding.FragmentCourseListBinding
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.list.adapter.CourseListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CourseListFragment :
	BaseFragment<FragmentCourseListBinding,
			CourseListViewModel.Listener,
			CourseListViewModel>(FragmentCourseListBinding::inflate),
	CourseListViewModel.Listener,
	CourseListAdapter.Listener {
	companion object {
		val TAG: String = CourseListFragment::class.java.simpleName
		const val TAG_COURSE_ID = "tag_course_id"
		const val TAG_START_SESSION = "tag_start_session"
	}

	@Inject
	lateinit var mainActivity: MainActivity

	@Inject
	lateinit var courseListAdapter: CourseListAdapter

	override fun setupViews(view: View) {
		// load views
		mainActivity.title = getString(R.string.courses)
		mainActivity.disableBackButton()

		// check if a course id was passed in, if so move to CourseDetailFragment and add to back stack
		arguments?.let {
			val courseId = it.getLong(TAG_COURSE_ID, -1)
			arguments = null
			if (courseId >= 0)
				viewModel.redirectToCourseDetail(courseId)
		}

		binding.fab.setOnClickListener {
			viewModel.fabClicked()
		}

		binding.recyclerView.apply {
			visibility = View.VISIBLE
			adapter = courseListAdapter
			layoutManager = LinearLayoutManager(context)
		}
	}

	override fun onStart() {
		super.onStart()
		courseListAdapter.registerListener(this)
		viewModel.registerListener(this)
		viewModel.fetchCourses()
	}

	override fun onStop() {
		super.onStop()
		courseListAdapter.unregisterListener(this)
		viewModel.unregisterListener(this)
	}

	// region ViewModel listeners ------------------------------------------------------------------
	override fun showCourses(courses: List<CourseRoom>, languages: List<LanguageRoom>) {
		courseListAdapter.loadData(courses, languages)
	}

	override fun showNoCourses() {
		binding.noCoursesText.visibility = View.VISIBLE
	}
	// endregion ViewModel listeners ---------------------------------------------------------------

	// region CourseListAdapter listeners ----------------------------------------------------------
	override fun onCourseClicked(course: CourseRoom) {
		viewModel.redirectToCourseDetail(course.id)
	}
	// endregion CourseListAdapter listeners -------------------------------------------------------
}