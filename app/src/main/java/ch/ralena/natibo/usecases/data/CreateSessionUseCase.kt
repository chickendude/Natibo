package ch.ralena.natibo.usecases.data

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import java.lang.Integer.max
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
	private val courseRepository: CourseRepository,
	private val sessionRepository: SessionRepository
) {
	suspend fun createSessionIfNecessary(course: CourseRoom) {
		val previousSession = sessionRepository.fetchSession(course.sessionId)
		if (previousSession?.isCompleted == false) return

		// Advance schedule starting sentence if we're starting a new session
		if (previousSession?.isCompleted == true) {
			val scheduleIndex = course.schedule.curSentenceIndex
			val sentencesStudied =
				previousSession.sentenceIndices.split(",").filter { it.toInt() >= scheduleIndex }
					.distinct().size
			course.schedule.curSentenceIndex = scheduleIndex + sentencesStudied
		}

		val numSessions = courseRepository.countSessions(course.id)
		val session = SessionRoom(
			index = numSessions + 1,
			progress = 0,
			courseId = course.id,
			sentenceIndices = getSentenceIndices(course)
		)
		course.sessionId = sessionRepository.createSession(session)
		courseRepository.updateCourse(course)
	}

	private fun getSentenceIndices(course: CourseRoom): String {
		val schedule = course.schedule
		val reviewPattern = schedule.reviewPattern.split(" ").map { it.toInt() }

		val startingIndex = schedule.curSentenceIndex
		val reviewSentences = mutableListOf<Int>()
		reviewPattern.drop(1).forEachIndexed { index, numTimes ->
			// Calculate start/end sentence indices and stop looping if the end is <= 1
			var start = startingIndex - (index + 1) * schedule.numSentences
			val end = start + schedule.numSentences
			if (end <= 1) return@forEachIndexed
			start = max(1, start)

			// Add sentences according to the review pattern (tells us how many times a sentence
			// should be studied).
			val sentences = mutableListOf<Int>()
			repeat(numTimes) { sentences.addAll(start until end) }
			sentences.shuffle()
			shuffleDuplicates(sentences)
			reviewSentences.addAll(sentences)
		}

		// First time sentences are seen they should be in order
		val initialSentences =
			(startingIndex until startingIndex + schedule.numSentences).toMutableList()

		// Review of new sentences should be randomized
		val sentences = mutableListOf<Int>()
		repeat(reviewPattern[0] - 1) { sentences.addAll(initialSentences) }
		sentences.shuffle()
		sentences.addAll(0, initialSentences)
		shuffleDuplicates(sentences, schedule.numSentences)
		sentences.addAll(0, reviewSentences)

		return sentences.joinToString(separator = ",")
	}

	// region Helper functions ---------------------------------------------------------------------
	private fun shuffleDuplicates(indices: MutableList<Int>, numNewSentences: Int = 1) {
		// Remove sentence indices that are duplicated
		val repeatedSentences = mutableListOf<Int>()
		indices.forEachIndexed { i, sentenceIndex ->
			if (i > 0 && indices[i - 1] == sentenceIndex) {
				repeatedSentences.add(sentenceIndex)
				indices[i] = -1
			}
		}
		indices.removeAll { it == -1 }

		// Insert indices that were duplicates back into list where they won't be repeated
		repeatedSentences.forEach { sentence ->
			var wasInserted = false
			for (index in numNewSentences until indices.size) {
				if (indices[index] != sentence && indices[index - 1] != sentence) {
					indices.add(index, sentence)
					wasInserted = true
					break
				}
			}
			// If we couldn't find a place for it, just add it to the end.
			// Should only be an issue with really small sizes, e.g. 1-2 unique sentences
			if (!wasInserted) indices.add(sentence)
		}
	}
	// endregion Helper functions ------------------------------------------------------------------
}