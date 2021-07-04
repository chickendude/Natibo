package ch.ralena.natibo.data.room

import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.`object`.*
import ch.ralena.natibo.data.room.dao.CourseDao
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.executeTransactionAwait
import java.util.*
import javax.inject.Inject

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
		baseLanguageId: Long,
		targetLanguageId: Long?,
		packName: String
	): Long {
		val scheduleRoom = ScheduleRoom(
			numSentencesPerDay,
			startingSentence,
			order,
			dailyReviews.joinToString(" ")
		)
		val courseRoom =
			CourseRoom(title, baseLanguageId, targetLanguageId, packName, scheduleRoom, 0)
		val courseId = courseDao.insert(courseRoom)

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

		return courseId
	}

	/**
	 * Fetches a single course.
	 *
	 * This fetches a single course and returns [Result.Success] if it was successful, otherwise
	 * it returns [Result.Failure].
	 *
	 * @param courseId The ID of the course to look for.
	 * @return [Result.Success] if it was successful, otherwise [Result.Failure].
	 */
	suspend fun fetchCourse(courseId: Long): Result<CourseRoom> {
		val course = courseDao.getCourseById(courseId)
		return if (course == null)
			Result.Failure(R.string.course_not_found)
		else
			Result.Success(course)
	}

	suspend fun deleteCourse(course: CourseRoom) {
		courseDao.delete(course)
	}

	suspend fun updateCourse(course: CourseRoom) {
		courseDao.update(course)
	}

	suspend fun countSessions(courseId: Long): Int = courseDao.getSessionCount(courseId)

	// region Old Realm stuff
	/**
	 * Toggles a pack in a course.
	 *
	 * If the pack is in the course it will be removed, otherwise it will be added.
	 *
	 * @param packId The ID of the pack to toggle.
	 * @param courseId The course ID of the pack which should be toggled.
	 */
	suspend fun togglePackInCourse(packId: String, courseId: Long) {
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
	// endregion

	// region Helper function ----------------------------------------------------------------------
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
// endregion Helper functions ----------------------------------------------------------------------

}