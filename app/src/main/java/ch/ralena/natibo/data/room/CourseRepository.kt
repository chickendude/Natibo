package ch.ralena.natibo.data.room

import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.`object`.*
import ch.ralena.natibo.data.room.dao.CourseDao
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.executeTransactionAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

/**
 * Repository for obtaining [Course] data.
 */
class CourseRepository @Inject constructor(
	private val realm: Realm,
	private val courseDao: CourseDao
) {
	/**
	 * Fetches all courses in the database.
	 *
	 * @return [List] containing all courses in the database.
	 */
	suspend fun fetchCourses(): List<CourseRoom> = courseDao.getAll()

	suspend fun createCourse(
		order: String,
		numSentencesPerDay: Int,
		startingSentence: Int,
		dailyReviews: List<String>,
		title: String,
		baseLanguageCode: String,
		targetLanguageCode: String
	): CourseRoom {
		val scheduleRoom = ScheduleRoom(
			numSentencesPerDay,
			startingSentence,
			order,
			dailyReviews.joinToString(" ")
		)
		val courseRoom = CourseRoom(title, baseLanguageCode, targetLanguageCode, scheduleRoom, null)
		courseDao.insert(courseRoom)

//		// --- begin transaction
//		realm.beginTransaction()
//
//		// Create sentence schedule
//		val schedule =
//			realm.createObject(Schedule::class.java, UUID.randomUUID().toString()).apply {
//				this.order = order
//				numSentences = numSentencesPerDay
//				sentenceIndex = startingSentence - 1
//				for (review in dailyReviews)
//					reviewPattern.add(review.toInt())
//			}
//
//		// Build course
//		val course = realm.createObject(Course::class.java, UUID.randomUUID().toString()).apply {
//			this.title = title
//			this.languages.clear()
//			this.languages.addAll(languages)
//			pauseMillis = 1000
//			this.schedule = schedule
//			// TODO: make sure triangulation packs are handled
//			packs = languages.first().getMatchingPacks(languages.last())
//			buildFirstDay(this)
//		}
//		realm.commitTransaction()
//		// --- end transaction
//		for (set in course.currentDay.sentenceSets)
//			set.buildSentences(realm)

		return courseRoom
	}

	/**
	 * Toggles a pack in a course.
	 *
	 * If the pack is in the course it will be removed, otherwise it will be added.
	 *
	 * @param packId The ID of the pack to toggle.
	 * @param courseId The course ID of the pack which should be toggled.
	 */
	suspend fun togglePackInCourse(packId: String, courseId: Int) {
		val r = Realm.getDefaultInstance()
		r.executeTransactionAwait {
			val course = it.where(Course::class.java).equalTo("id", courseId).findFirst()!!
			val pack = it.where(Pack::class.java).equalTo("id", packId).findFirst()!!
			if (course.packs.contains(pack))
				course.packs.remove(pack)
			else
				course.packs.add(pack)
		}
	}

	/**
	 * Fetches a single course.
	 *
	 * This fetches a single course and notifies [callback] whether it was successful or not.
	 *
	 * @param courseId The ID of the course to look for.
	 * @param callback If found, the course will be passed to this wrapped in [Result.Success],
	 * otherwise [Result.Failure] will be passed in.
	 */
	suspend fun fetchCourse(courseId: Int): Result<CourseRoom> {
		val course = courseDao.getCourseById(courseId)
		return if (course == null)
			Result.Failure(R.string.course_not_found)
		else
			Result.Success(course)
	}

	/**
	 * Prepares the sentences for the next day of study.
	 *
	 * @param course The course to prepare
	 */
	fun prepareNextDay(course: Course) {
		// add current day to past days

		// add current day to past days
		if (course.currentDay?.isCompleted == true)
			realm.executeTransaction { course.pastDays.add(course.currentDay) }

		// create a new day
		realm.executeTransaction { r: Realm ->
			val day = r.createObject(Day::class.java, UUID.randomUUID().toString())

			// add the sentence sets from the current day to the next day
			if (course.currentDay != null && course.currentDay.isCompleted) {
				day.sentenceSets.addAll(course.currentDay.sentenceSets)

				// move yesterday's new words to the front of the reviews
				val lastSet = day.sentenceSets.last()
				day.sentenceSets.remove(lastSet)
				day.sentenceSets.add(0, lastSet)
			}
			val reviewPattern: RealmList<Int> = course.schedule.getReviewPattern()
			val numSentences: Int = course.schedule.numSentences
			val sentenceIndex: Int = course.schedule.sentenceIndex
			course.schedule.sentenceIndex = sentenceIndex + numSentences

			// create new set of sentences based off the schedule
			val sentenceSet = SentenceSet()
			sentenceSet.sentenceSet =
				getSentenceGroups(sentenceIndex, numSentences, course.languages, course.packs)
			sentenceSet.reviews = reviewPattern
			sentenceSet.isFirstDay = true
			sentenceSet.order = course.schedule.order

			// add sentence set to list of sentencesets for the next day's studies
			day.sentenceSets.add(sentenceSet)
			day.isCompleted = false
			day.pauseMillis = course.pauseMillis
			day.setPlaybackSpeed(course.playbackSpeed)
			course.currentDay = day
		}
		val emptySentenceSets: MutableList<SentenceSet> = ArrayList()
		for (set in course.currentDay.sentenceSets) {
			// create sentence set and mark it to be deleted if it is empty
			if (!set.buildSentences(realm)) {
				emptySentenceSets.add(set)
			}
		}

		// delete the sentence sets with no reviews left
		realm.executeTransaction {
			course.currentDay.sentenceSets.removeAll(emptySentenceSets)
		}
	}

	suspend fun deleteCourse(course: CourseRoom) {
		courseDao.delete(course)
	}

	// region Helper functions----------------------------------------------------------------------
	private fun buildFirstDay(course: Course) {
		val day = realm.createObject(Day::class.java, UUID.randomUUID().toString())

		val reviewPattern: RealmList<Int> = course.schedule.reviewPattern
		val numSentences = course.schedule.numSentences
		var sentenceIndex: Int = course.schedule.sentenceIndex

		for (pattern in reviewPattern) {
			val reviews = RealmList<Int>().apply {
				addAll(reviewPattern.subList(reviewPattern.indexOf(pattern), reviewPattern.size))
			}
			val sentenceSet = SentenceSet().apply {
				this.reviews = reviews
				order = course.schedule.order
				sentenceSet =
					getSentenceGroups(sentenceIndex, numSentences, course.languages, course.packs)
			}

			sentenceIndex -= numSentences
			if (sentenceIndex < 0)
				break
			day.sentenceSets.add(sentenceSet)
		}

		// Move first sentence set (new sentences) to end
		if (day.sentenceSets.size > 0) {
			day.sentenceSets.add(day.sentenceSets.removeFirst())
			day.sentenceSets.last()?.isFirstDay = true
		}

		// add sentence set to list of sentencesets for the next day's studies
		day.isCompleted = false
		day.pauseMillis = course.pauseMillis
		day.setPlaybackSpeed(course.playbackSpeed)
		course.currentDay = day
	}

	private fun getSentenceGroups(
		index: Int,
		numSentences: Int,
		languages: List<Language>,
		packs: List<Pack>
	): RealmList<SentenceGroup> {
		val sentenceGroups = RealmList<SentenceGroup>()

		// go through each pack
		for (language in languages) {
			var i = 0
			var sentenceIndex = index
			var sentencesToAdd = numSentences
			for (pack in getPacksPerLanguage(language, packs)) {
				val packSentences = pack.sentences
				if (sentenceIndex >= pack.sentences.size) sentenceIndex -= packSentences.size else {
					while (sentencesToAdd > 0) {
						if (sentenceIndex >= pack.sentences.size) break
						sentencesToAdd--
						val sentence = packSentences[sentenceIndex++]
						if (sentenceGroups.size <= i) {
							sentenceGroups.add(SentenceGroup())
						}
						sentenceGroups[i]!!.sentences.add(sentence)
						sentenceGroups[i++]!!.languages.add(language)
					}
				}
			}
		}
		return sentenceGroups
	}

	private fun getPacksPerLanguage(language: Language, packs: List<Pack>): RealmList<Pack> {
		val results = RealmList<Pack>()
		for (pack in packs) {
			if (language.hasBook(pack.book)) results.add(language.getPack(pack.book))
		}
		return results
	}
// endregion Helper functions-------------------------------------------------------------------

}