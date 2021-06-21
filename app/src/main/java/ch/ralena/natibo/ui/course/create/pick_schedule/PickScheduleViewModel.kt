package ch.ralena.natibo.ui.course.create.pick_schedule

import androidx.annotation.IdRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.math.min

class PickScheduleViewModel @Inject constructor(
	private val screenNavigator: ScreenNavigator,
	private val languageRepository: LanguageRepository,
	private val courseRepository: CourseRepository,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<PickScheduleViewModel.Listener>() {
	interface Listener {
		fun setCourseTitle(title: String)
	}

	val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private val languages = ArrayList<LanguageRoom>()

	companion object {
		const val MAX_REPETITIONS = 99
	}

	fun fetchLanguages(languageIds: LongArray?) {
		// TODO: throw error if null or empty
		if (languageIds == null || languageIds.isEmpty())
			return
		coroutineScope.launch {
			languages.clear()
			languageIds.forEach {
				languageRepository.fetchLanguage(it)?.run {
					languages.add(this)
				}
			}
			val title = languages.joinToString(" → ") { it.name }
			withContext(dispatcherProvider.main()) {
				listeners.forEach { it.setCourseTitle(title) }
			}
		}
	}

	fun createCourse(
		dailyReviewsText: String,
		numSentencesPerDay: Int,
		startingSentence: Int,
		title: String,
		@IdRes chorusId: Int
	) {
		if (numSentencesPerDay <= 0)
			return

		val chorus = when (chorusId) {
			R.id.chorus_all_radio -> Course.Chorus.ALL
			R.id.chorus_new_radio -> Course.Chorus.NEW
			else -> Course.Chorus.NONE
		}

		// Split up reviews and check if there are any invalid inputs
		val dailyReviews = dailyReviewsText.split(" / ")
		if (dailyReviews.any { it.toIntOrNull() == null })
			return

		// "base-target-target" if chorus enabled, otherwise "base-target"
		// TODO: handle Course.Chorus.NEW, perhaps create "order_review" and "order_new"
		val order = languages.mapIndexed { index, _ ->
			if (chorus == Course.Chorus.ALL && (index > 0 || languages.size == 1))
				"$index$index"
			else
				"$index"
		}.joinToString("")

		val baseLanguageCode = languages.first().code
		val targetLanguageCode = languages.last().code

		coroutineScope.launch(dispatcherProvider.main()) {
			val courseId = withContext(dispatcherProvider.io()) {
				courseRepository.createCourse(
					order,
					numSentencesPerDay,
					startingSentence,
					dailyReviews,
					title,
					baseLanguageCode,
					targetLanguageCode
				)
			}
			// TODO: perhaps wait for response from courseRepository and send signal back to fragment
			//  sharing whether or not it was successful
			screenNavigator.toCourseListFragment(courseId)
		}
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
				" "
			)

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