package ch.ralena.natibo.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.course.list.CourseListFragment
import ch.ralena.natibo.ui.course.detail.CourseDetailFragment
import ch.ralena.natibo.ui.course.create.pick_language.PickLanguagesFragment
import ch.ralena.natibo.ui.course.create.pick_schedule.PickScheduleFragment
import ch.ralena.natibo.ui.language.list.LanguageListFragment
import ch.ralena.natibo.ui.fragment.MainSettingsFragment
import ch.ralena.natibo.ui.language.detail.LanguageDetailFragment
import ch.ralena.natibo.ui.sentences.SentenceListFragment
import ch.ralena.natibo.ui.settings_course.CourseSettingsFragment
import ch.ralena.natibo.ui.study.insession.StudySessionFragment
import io.realm.Realm
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
	private val fragmentManager: FragmentManager,
	private val realm: Realm,
	private val activity: MainActivity
) {
	fun toCourseDetailFragment(courseId: Long) {
		val fragment = CourseDetailFragment()
		fragment.arguments = Bundle().apply {
			putLong(CourseDetailFragment.TAG_COURSE_ID, courseId)
		}
		loadFragment(fragment, CourseDetailFragment.TAG)
	}

	fun toCourseCreateFragment() {
		if (realm.where(Language::class.java).count() == 0L) {
			activity.snackBar(R.string.no_languages)
		} else {
			loadFragment(PickLanguagesFragment(), PickLanguagesFragment.TAG)
		}
	}

	fun toCourseListFragment(courseId: Long? = null) {
		val fragment = CourseListFragment()
		courseId?.let {
			clearBackStack()
			fragment.arguments = Bundle().apply {
				putLong(CourseListFragment.TAG_COURSE_ID, courseId)
			}
		}
		loadFragment(fragment, CourseListFragment.TAG)
	}

	fun toCoursePreparationFragment(languageIds: List<Long>) {
		val fragment = PickScheduleFragment()
		fragment.arguments = Bundle().apply {
			// add language ids in a bundle
			putLongArray(PickScheduleFragment.TAG_LANGUAGE_IDS, languageIds.toLongArray())
		}
		loadFragment(fragment, PickScheduleFragment.TAG)
	}

	fun toCourseSettingsFragment(courseId: Long) {
		val fragment = CourseSettingsFragment()

		// load fragment ID into fragment arguments
		fragment.arguments = Bundle().apply {
			putLong(CourseSettingsFragment.KEY_ID, courseId)
		}
		loadFragment(fragment, CourseSettingsFragment.TAG)
	}

	fun toLanguageListFragment() {
		loadFragment(LanguageListFragment(), LanguageListFragment.TAG)
	}

	fun toLanguageDetailsFragment(languageId: Long) {
		val fragment = LanguageDetailFragment()
		fragment.arguments = Bundle().apply {
			putLong(LanguageDetailFragment.TAG_LANGUAGE_ID, languageId)
		}
		loadFragment(fragment, LanguageDetailFragment.TAG)
	}

	fun toMainSettingsFragment() {
		loadFragment(MainSettingsFragment(), MainSettingsFragment.TAG)
	}

	fun toSentenceListFragment(packId: Long, languageId: String) {
		val fragment = SentenceListFragment()
		fragment.arguments = Bundle().apply {
			putString(SentenceListFragment.TAG_LANGUAGE_ID, languageId)
			putLong(SentenceListFragment.TAG_BASE_PACK_ID, packId)
		}
		loadFragment(fragment, SentenceListFragment.TAG)
	}

	fun toStudySessionFragment(courseId: Long) {
		val fragment = StudySessionFragment()
		fragment.arguments = Bundle().apply {
			putLong(StudySessionFragment.KEY_COURSE_ID, courseId)
		}
		loadFragment(fragment, StudySessionFragment.TAG)
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