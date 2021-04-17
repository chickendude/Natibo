package ch.ralena.natibo.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.course.list.CourseListFragment
import ch.ralena.natibo.ui.course.detail.CourseDetailFragment
import ch.ralena.natibo.ui.course.create.pick_language.PickLanguagesFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.PickScheduleFragment
import ch.ralena.natibo.ui.language.list.LanguageListFragment
import ch.ralena.natibo.ui.fragment.MainSettingsFragment
import ch.ralena.natibo.ui.language.detail.LanguageDetailFragment
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
			loadFragment(fragment, CourseDetailFragment.TAG)
		}
	}

	fun toCourseCreateFragment() {
		if (realm.where(Language::class.java).count() == 0L) {
			activity.snackBar(R.string.no_languages)
		} else {
			loadFragment(PickLanguagesFragment(), PickLanguagesFragment.TAG)
		}
	}

	fun toCourseListFragment(courseId: String? = null) {
		val fragment = CourseListFragment()
		courseId?.let {
			clearBackStack()
			val bundle = Bundle()
			bundle.putString(CourseListFragment.TAG_COURSE_ID, courseId)
			fragment.arguments = bundle
		}
		loadFragment(fragment, CourseListFragment.TAG)
	}

	fun toLanguageListFragment() {
		loadFragment(LanguageListFragment(), LanguageListFragment.TAG)
	}

	fun toLanguageDetailsFragment(languageId: String) {
		val fragment = LanguageDetailFragment()
		fragment.arguments = Bundle().apply {
			putString(LanguageDetailFragment.TAG_LANGUAGE_ID, languageId)
		}
		loadFragment(fragment, LanguageDetailFragment.TAG)
	}

	fun toMainSettingsFragment() {
		loadFragment(MainSettingsFragment(), MainSettingsFragment.TAG)
	}

	fun toCoursePreparationFragment(languageIds: List<String>) {
		val fragment = PickScheduleFragment()
		// add language ids in a bundle
		val bundle = Bundle()
		bundle.putStringArray(PickScheduleFragment.TAG_LANGUAGE_IDS, languageIds.toTypedArray())
		fragment.arguments = bundle
		loadFragment(fragment, PickScheduleFragment.TAG)
	}

	// Private functions

	private fun loadFragment(fragment: Fragment, name: String) {
		val transaction = fragmentManager
				.beginTransaction()
				.replace(R.id.fragmentPlaceHolder, fragment)

		// make sure fragment isn't added to back stack twice
		val backStackCount = fragmentManager.backStackEntryCount
		if (backStackCount > 0) {
			val entry = fragmentManager.getBackStackEntryAt(backStackCount - 1)
			if (entry.name != name)
				transaction.addToBackStack(name)
		} else if (name != CourseListFragment.TAG)
			transaction.addToBackStack(name)

		transaction.commit()
	}

	// TODO: Make private when MainActivity no longer depends on it
	fun clearBackStack() {
		if (fragmentManager.backStackEntryCount > 0) {
			val entryId = fragmentManager.getBackStackEntryAt(0).id
			fragmentManager.popBackStackImmediate(entryId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
		}
	}
}