package ch.ralena.natibo.ui.course.create.pick_schedule

import androidx.annotation.IdRes
import ch.ralena.natibo.R
import ch.ralena.natibo.data.Chorus
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.data.room.PackRepository
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.ui.base.BaseViewModel
import ch.ralena.natibo.utils.DispatcherProvider
import ch.ralena.natibo.utils.ScreenNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.min

private const val MAX_REPETITIONS = 99

class PickScheduleViewModel @Inject constructor(
	private val screenNavigator: ScreenNavigator,
	private val languageRepository: LanguageRepository,
	private val courseRepository: CourseRepository,
	private val packRepository: PackRepository,
	private val dispatcherProvider: DispatcherProvider
) : BaseViewModel<PickScheduleViewModel.Listener>() {
	interface Listener {
		fun setCourseTitle(title: String)
	}

	private val coroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.default())

	private lateinit var nativeLanguage: LanguageRoom
	private var targetLanguage: LanguageRoom? = null
	private lateinit var pack: PackRoom

	fun fetchData(nativeId: Long, targetId: Long, packId: Long) {
		// TODO: Throw error if nativeLanguage comes back null
		if (nativeId < 0 || packId < 0)
			return
		coroutineScope.launch {
			nativeLanguage = languageRepository.fetchLanguage(nativeId)!!
			targetLanguage = languageRepository.fetchLanguage(targetId)

			pack = packRepository.fetchPack(packId)!!
			val languageTitle =
				if (targetLanguage == null)
					nativeLanguage.name
				else
					"${nativeLanguage.name} → ${targetLanguage!!.name}"
			val title = "$languageTitle : ${pack.name}"
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
			R.id.chorus_all_radio -> Chorus.ALL
			R.id.chorus_new_radio -> Chorus.NEW
			else -> Chorus.NONE
		}

		// Split up reviews and check if there are any invalid inputs
		val dailyReviews = dailyReviewsText.split(" / ")
		if (dailyReviews.any { it.toIntOrNull() == null })
			return

		// "base-target-target" if chorus enabled, otherwise "base-target"
		// TODO: handle Course.Chorus.NEW, perhaps create "order_review" and "order_new"
		val order = listOf(nativeLanguage, targetLanguage).mapIndexed { index, _ ->
			if (chorus == Chorus.ALL && (index > 0 || targetLanguage == null))
				"$index$index"
			else
				"$index"
		}.joinToString("")

		val nativeId = nativeLanguage.id
		val targetId = targetLanguage?.id

		coroutineScope.launch(dispatcherProvider.main()) {
			val courseId = withContext(dispatcherProvider.io()) {
				courseRepository.createCourse(
					order,
					numSentencesPerDay,
					startingSentence,
					dailyReviews,
					title,
					nativeId,
					targetId,
					pack.id
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