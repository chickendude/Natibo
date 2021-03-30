package ch.ralena.natibo.utils

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import ch.ralena.natibo.R
import ch.ralena.natibo.`object`.Course
import ch.ralena.natibo.`object`.Language
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.course_list.CourseListFragment
import ch.ralena.natibo.ui.fragment.CourseDetailFragment
import ch.ralena.natibo.ui.fragment.CoursePickLanguageFragment
import io.realm.Realm
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
		private val fragmentManager: FragmentManager,
		private val realm: Realm,
		private val activity: MainActivity
) {
	fun toCourseDetailFragment(courseId: String?) {
		val course = realm.where(Course::class.java).equalTo("id", courseId).findFirst()
		course?.run {
			val fragment = CourseDetailFragment()
			val bundle = Bundle()
			bundle.putString(CourseDetailFragment.TAG_COURSE_ID, course.getId())
			fragment.setArguments(bundle)
			fragmentManager
					.beginTransaction()
					.replace(R.id.fragmentPlaceHolder, fragment)
					.addToBackStack(null)
					.commit()
		}
	}

	fun toCourseCreateFragment() {
		if (realm.where(Language::class.java).count() == 0L) {
			activity.snackBar(R.string.no_languages)
		} else {
			val fragment = CoursePickLanguageFragment()
			fragmentManager
					.beginTransaction()
					.replace(R.id.fragmentPlaceHolder, fragment)
					.addToBackStack(null)
					.commit()
		}
	}
}