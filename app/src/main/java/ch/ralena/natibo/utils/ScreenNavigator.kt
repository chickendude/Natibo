package ch.ralena.natibo.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.LanguageRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScreenNavigator @Inject constructor(
	private val fragmentManager: FragmentManager,
	private val languageRepository: LanguageRepository,
	private val activity: MainActivity,
	private val dispatcherProvider: DispatcherProvider
) {
	private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	fun toCourseDetailFragment(courseId: Long) {
		val fragment = CourseDetailFragment()
		fragment.arguments = Bundle().apply {
			putLong(CourseDetailFragment.TAG_COURSE_ID, courseId)
		}
		loadFragment(fragment, CourseDetailFragment.TAG)
	}

	fun toCourseCreateFragment() {
		coroutineScope.launch {
			if (languageRepository.fetchLanguages().isEmpty()) {
				activity.snackBar(R.string.no_languages)
			} else {
				withContext(dispatcherProvider.main()) {
					loadFragment(PickLanguagesFragment(), PickLanguagesFragment.TAG)
				}
			}
		}
	}

	fun toCourseListFragment(courseId: Long? = null) {
		val fragment = CourseListFragment()
		courseId?.run {
			clearBackStack()
			fragment.arguments = Bundle().apply {
				putLong(CourseListFragment.TAG_COURSE_ID, courseId)
			}
		}
		loadFragment(fragment, CourseListFragment.TAG, addToBackStack = false)
	}

	fun toCoursePreparationFragment(nativeId: Long, targetId: Long?, packId: Long) {
		val fragment = PickScheduleFragment()
		fragment.arguments = Bundle().apply {
			// add language ids in a bundle
			putLong(PickScheduleFragment.TAG_NATIVE_ID, nativeId)
			putLong(PickScheduleFragment.TAG_TARGET_ID, targetId ?: -1)
			putLong(PickScheduleFragment.TAG_PACK_ID, packId)
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

	fun toLanguageListFragment(addToBackStack: Boolean = true) {
		loadFragment(LanguageListFragment(), LanguageListFragment.TAG, addToBackStack)
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

	fun toSentenceListFragment(packId: Long, languageId: Long) {
		val fragment = SentenceListFragment()
		fragment.arguments = Bundle().apply {
			putLong(SentenceListFragment.TAG_LANGUAGE_ID, languageId)
			putLong(SentenceListFragment.TAG_PACK_ID, packId)
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

	// region Helper functions----------------------------------------------------------------------
	private fun loadFragment(fragment: Fragment, name: String, addToBackStack: Boolean = true) {
		val transaction = fragmentManager
			.beginTransaction()
			.replace(R.id.fragmentPlaceHolder, fragment)

		if (addToBackStack) {
			// make sure fragment isn't added to back stack twice
			val backStackCount = fragmentManager.backStackEntryCount
			if (backStackCount > 0) {
				val entry = fragmentManager.getBackStackEntryAt(backStackCount - 1)
				if (entry.name != name)
					transaction.addToBackStack(name)
			} else transaction.addToBackStack(name)
		}

		transaction.commit()
	}

	// TODO: Make private when MainActivity no longer depends on it
	fun clearBackStack() {
		if (fragmentManager.backStackEntryCount > 0) {
			val entryId = fragmentManager.getBackStackEntryAt(0).id
			fragmentManager.popBackStackImmediate(entryId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
		}
	}
	// endregion Helper functions-------------------------------------------------------------------
}