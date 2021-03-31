package ch.ralena.natibo.ui.course_list

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.adapter.CourseListAdapter
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.utils.ScreenNavigator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.RealmResults
import javax.inject.Inject

class CourseListFragment : BaseFragment<CourseListViewModel.Listener, CourseListViewModel>(), CourseListViewModel.Listener {
	@Inject
	lateinit var screenNavigator: ScreenNavigator

	@Inject
	lateinit var mainActivity: MainActivity

	private lateinit var recyclerView: RecyclerView
	private lateinit var noCoursesText: TextView

	private var courses: RealmResults<Course>? = null

	companion object {
		val TAG: String = CourseListFragment::class.java.simpleName
		const val TAG_COURSE_ID = "tag_course_id"
		const val TAG_START_SESSION = "tag_start_session"
	}

	override fun provideLayoutId() = R.layout.fragment_course_list

	override fun setupViews(view: View) {
		// load views
		noCoursesText = view.findViewById(R.id.noCoursesText)
		recyclerView = view.findViewById(R.id.recyclerView)
		mainActivity.disableBackButton()
		mainActivity.setMenuToCourses()

		// check if a course id was passed in, if so move to CourseDetailFragment and add to back stack
		arguments?.let {
			val courseId = it.getString(TAG_COURSE_ID)
			arguments = null
			screenNavigator.toCourseDetailFragment(courseId)
		}

		view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
			screenNavigator.toCourseCreateFragment()
		}
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
		viewModel.registerListener(this)
	}

	override fun onStart() {
		super.onStart()
		mainActivity.title = getString(R.string.courses)
		viewModel.registerListener(this)
		viewModel.fetchCourses()
	}

	override fun onStop() {
		super.onStop()
		viewModel.unregisterListener(this)
	}

	override fun showCourses(courses: RealmResults<Course>) {
		this.courses = courses
		recyclerView.apply {
			visibility = View.VISIBLE

			// TODO: Switch to dependency injection and use method to update courses
			val courseListAdapter = CourseListAdapter(courses)
			adapter = courseListAdapter
			layoutManager = LinearLayoutManager(context)
			courseListAdapter.asObservable().subscribe { course: Course? -> screenNavigator.toCourseDetailFragment(course?.id) }
		}
	}

	override fun showNoCourses() {
		noCoursesText.visibility = View.VISIBLE
	}
}