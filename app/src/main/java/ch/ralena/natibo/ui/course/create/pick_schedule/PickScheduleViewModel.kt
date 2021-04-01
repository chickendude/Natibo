package ch.ralena.natibo.ui.course.create.pick_schedule

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.Schedule
import ch.ralena.natibo.di.module.SelectedLanguages
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import io.realm.RealmList
import java.util.*
import javax.inject.Inject

class PickScheduleViewModel @Inject constructor(
		private val screenNavigator: ScreenNavigator,
		private val languageRepository: LanguageRepository,
		private val courseRepository: CourseRepository
) : BaseViewModel<PickScheduleViewModel.Listener>() {
	interface Listener {
		fun onLanguagesLoaded(languages: List<Language>)
		fun onCourseCreated(course: Course)
	}

	private lateinit var languageIds: Array<String>

	fun fetchLanguages() {
		val languages = languageRepository.fetchLanguagesFromIds(languageIds)
		for (l in listeners)
			l.onLanguagesLoaded(languages)
	}

	fun createCourse(order: String, numSentencesPerDay: Int, dailyReviews: Array<String>, title: String, languages: List<Language>) {
		val course = courseRepository.createCourse(order, numSentencesPerDay, dailyReviews, title, languages)
		for (l in listeners)
			l.onCourseCreated(course)
	}

	fun saveLanguageIds(it: Array<String>) {
		languageIds = it
	}
}