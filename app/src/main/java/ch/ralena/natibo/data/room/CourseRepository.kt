package ch.ralena.natibo.data.room

import ch.ralena.natibo.R
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.`object`.*
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.executeTransactionAwait
import java.util.*
import javax.inject.Inject

/**
 * Repository for obtaining [Course] data.
 */
class CourseRepository @Inject constructor(private val realm: Realm) {
	/**
	 * Fetches all courses in the database.
	 *
	 * @return `RealmResults` containing all courses in the database.
	 */
	fun fetchCourses(): RealmResults<Course> = realm.where(Course::class.java).findAll()

	fun createCourse(
		order: String,
		numSentencesPerDay: Int,
		startingSentence: Int,
		dailyReviews: List<String>,
		title: String,
		languages: List<Language>
	): Course {
		// --- begin transaction
		realm.beginTransaction()

		// Create sentence schedule
		val schedule =
			realm.createObject(Schedule::class.java, UUID.randomUUID().toString()).apply {
				this.order = order
				numSentences = numSentencesPerDay
				sentenceIndex = startingSentence - 1
				for (review in dailyReviews)
					reviewPattern.add(review.toInt())
			}

		// Build course
		val course = realm.createObject(Course::class.java, UUID.randomUUID().toString()).apply {
			this.title = title
			this.languages.clear()
			this.languages.addAll(languages)
			pauseMillis = 1000
			this.schedule = schedule
			// TODO: make sure triangulation packs are handled
			packs = languages.first().getMatchingPacks(languages.last())
			buildFirstDay(this)
		}
		realm.commitTransaction()
		// --- end transaction
		for (set in course.currentDay.sentenceSets)
			set.buildSentences(realm)

		return course
	}

	/**
	 * Toggles a pack in a course.
	 *
	 * If the pack is in the course it will be removed, otherwise it will be added.
	 *
	 * @param packId The ID of the pack to toggle.
	 * @param courseId The course ID of the pack which should be toggled.
	 */
	suspend fun togglePackInCourse(packId: String, courseId: String) {
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
	fun fetchCourse(courseId: String, callback: (result: Result<Course>) -> Unit) {
		val course = realm.where(Course::class.java).equalTo("id", courseId).findFirst()
		if (course == null)
			callback(Result.Failure(R.string.course_not_found))
		else
			callback(Result.Success(course))
	}

	fun deleteCourse(courseId: String) {
		realm.executeTransactionAsync {
			it.where(Course::class.java)
				.equalTo("id", courseId)
				.findFirst()
				?.deleteFromRealm()
		}
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
		day.sentenceSets.add(day.sentenceSets.removeFirst())
		day.sentenceSets.last()?.isFirstDay = true

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