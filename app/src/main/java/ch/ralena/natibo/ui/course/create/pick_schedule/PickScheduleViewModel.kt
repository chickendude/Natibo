package ch.ralena.natibo.ui.course.create.pick_schedule

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.ScreenNavigator
import javax.inject.Inject
import kotlin.math.min

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

	companion object {
		const val MAX_REPETITIONS = 99
	}

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

	fun getSchedulePatternFromString(string: String): String {
		var pattern = "? / ? / ?"
		if (string.isNotEmpty()) {
			// Split the string using the delimiters below
			val numberStrings = string.trim(' ').split(
					"*",
					".",
					",",
					"/",
					" ")

			// Convert list of Strings to list of Integers and clean out bad input.
			val numbers = numberStrings.map {
				val number = it.toIntOrNull()
				number?.let {
					min(MAX_REPETITIONS, number)
				}
			}.filterNotNull()

			pattern = numbers.joinToString(" / ")
		}
		return pattern
	}

	fun saveLanguageIds(it: Array<String>) {
		languageIds = it
	}
}