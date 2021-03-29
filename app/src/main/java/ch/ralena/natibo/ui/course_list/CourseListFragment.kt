package ch.ralena.natibo.ui.course_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.`object`.Course
import ch.ralena.natibo.`object`.Language
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.adapter.CourseListAdapter
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.fragment.CourseDetailFragment
import ch.ralena.natibo.ui.fragment.CoursePickLanguageFragment
import ch.ralena.natibo.utils.ScreenNavigator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.functions.Consumer
import io.realm.Realm
import io.realm.RealmResults
import javax.inject.Inject

class CourseListFragment : BaseFragment<CourseListViewModel.Listener, CourseListViewModel>() {
	@Inject
	lateinit var screenNavigator: ScreenNavigator

	var courses: RealmResults<Course>? = null
	private var realm: Realm? = null
//	private var activity: MainActivity? = null

	companion object {
		private val TAG = CourseListFragment::class.java.simpleName
		const val TAG_COURSE_ID = "tag_course_id"
		const val TAG_START_SESSION = "tag_start_session"
	}

	override fun provideLayoutId() = R.layout.fragment_course_list

	override fun setupViews(view: View) {

		// load schedules from database
		courses = viewModel.fetchCourses()

		// check if a course id was passed in, if so move to CourseDetailFragment and add to back stack
		arguments?.let {
			val courseId = it.getString(TAG_COURSE_ID)
			screenNavigator.toCourseDetailFragment(courseId)
			arguments = null
		}

		// load views
		val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
		val noCoursesText = view.findViewById<TextView>(R.id.noCoursesText)
		val fab: FloatingActionButton = view.findViewById(R.id.fab)
		if (courses!!.size == 0) {
			noCoursesText.setVisibility(View.VISIBLE)
			recyclerView.setVisibility(View.GONE)
		} else {
			// hide "No Courses" text
			noCoursesText.setVisibility(View.GONE)
			recyclerView.setVisibility(View.VISIBLE)

			// set up recyclerlist and adapter
			val adapter = CourseListAdapter(courses)
			recyclerView.setAdapter(adapter)
			val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
			recyclerView.setLayoutManager(layoutManager)
			adapter.asObservable().subscribe { course: Course? -> screenNavigator.toCourseDetailFragment(course?.id) }
		}

		// set up FAB
		fab.setOnClickListener { v: View? -> screenNavigator.toCourseCreateFragment() }
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	override fun onStart() {
		super.onStart()
		(getActivity() as MainActivity?)!!.setNavigationDrawerItemChecked(R.id.nav_courses)
		getActivity()!!.title = getString(R.string.courses)
	}
}