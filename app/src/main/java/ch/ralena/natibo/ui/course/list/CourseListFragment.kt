package ch.ralena.natibo.ui.course.list

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.databinding.FragmentCourseListBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.course.list.adapter.CourseListAdapter
import ch.ralena.natibo.utils.ScreenNavigator
import javax.inject.Inject

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
			val courseId = it.getString(TAG_COURSE_ID)
			arguments = null
			if (courseId != null)
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

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
		viewModel.registerListener(this)
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
	override fun showCourses(courses: List<Course>) {
		courseListAdapter.loadCourses(courses)
	}

	override fun showNoCourses() {
		binding.noCoursesText.visibility = View.VISIBLE
	}
	// endregion ViewModel listeners ---------------------------------------------------------------

	// region CourseListAdapter listeners ----------------------------------------------------------
	override fun onCourseClicked(course: Course) {
		viewModel.redirectToCourseDetail(course.id)
	}
	// endregion CourseListAdapter listeners -------------------------------------------------------
}