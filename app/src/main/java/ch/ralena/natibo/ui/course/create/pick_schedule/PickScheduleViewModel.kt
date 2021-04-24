package ch.ralena.natibo.ui.course.create.pick_schedule

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
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
		fun setCourseTitle(title: String)
	}

	private lateinit var languages: List<Language>

	companion object {
		const val MAX_REPETITIONS = 99
	}

	fun fetchLanguages(languageIds: Array<String>?) {
		// TODO: throw error if null or empty
		if (languageIds.isNullOrEmpty())
			return
		languages = languageRepository.fetchLanguagesFromIds(languageIds)
		val title = languages.map { it.longName }.joinToString(" → ")
		for (l in listeners)
			l.setCourseTitle(title)
	}

	fun createCourse(dailyReviewsText: String, numSentencesPerDay: Int, title: String, isChorus: Boolean) {
		if (numSentencesPerDay <= 0)
			return

		// Split up reviews and check if there are any invalid inputs
		val dailyReviews = dailyReviewsText.split(" / ")
		if (dailyReviews.any { it.toIntOrNull() == null })
			return

		// "base-target-target" if chorus enabled, otherwise "base-target"
		val order = languages.mapIndexed { i, language ->
			if (isChorus && (i > 0 || languages.size == 1))
				"$i$i"
			else
				"$i"
		}.joinToString("")

		val course = courseRepository.createCourse(order, numSentencesPerDay, dailyReviews, title, languages)

		screenNavigator.toCourseListFragment(course.id)
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

	fun getSentencesPerDayFromString(string: String) =
			string.toIntOrNull()?.let {
				min(100, it)
			} ?: 0
}