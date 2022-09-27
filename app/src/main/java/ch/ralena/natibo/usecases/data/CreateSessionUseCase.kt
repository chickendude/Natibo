package ch.ralena.natibo.usecases.data

import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.SessionRoom
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
	private val courseRepository: CourseRepository,
	private val sessionRepository: SessionRepository
) {
	suspend fun createSessionIfNecessary(course: CourseRoom) {
		val previousSession = sessionRepository.fetchSession(course.sessionId)
		if (previousSession?.isCompleted == false) return

		val numSessions = courseRepository.countSessions(course.id)
		val session = SessionRoom(
			index = numSessions + 1,
			progress = course.schedule.curSentenceIndex,
			courseId = course.id,
			sentenceIndices = getSentenceIndices(course)
		)
		course.sessionId = sessionRepository.createSession(session)
		courseRepository.updateCourse(course)
	}

	private fun getSentenceIndices(course: CourseRoom): String {
		val schedule = course.schedule
		val startingIndex = schedule.curSentenceIndex

		// First time sentences are seen they should be in order
		val initialSentences = mutableListOf<Int>()
		for (i in 0 until schedule.numSentences) {
			initialSentences.add(startingIndex + i)
		}

		// Review of new sentences should be randomized
		val sentences = mutableListOf<Int>()
		val numTimes = schedule.reviewPattern.split(' ').first().toInt() - 1
		for (i in 0 until schedule.numSentences) {
			repeat(numTimes) { sentences.add(startingIndex + i) }
		}
		sentences.shuffle()
		sentences.addAll(0, initialSentences)

		// make sure no sentences are repeated twice in a row
		val repeatedSentences = mutableListOf<Int>()
		sentences.forEachIndexed { i, sentence ->
			if (i > 0 && sentences[i - 1] == sentence) {
				repeatedSentences.add(sentence)
				sentences[i] = -1
			}
		}
		sentences.removeAll { it == -1 }

		// Insert sentences back into list where they won't be repeated
		repeatedSentences.forEach { sentence ->
			var wasInserted = false
			for (index in 1 until sentences.size) {
				if (sentences[index] != sentence && sentences[index - 1] != sentence) {
					sentences.add(index, sentence)
					wasInserted = true
					break
				}
			}
			// If we couldn't find a place for it, just add it to the end.
			// Should only be an issue with really small sizes, e.g. 1-2 unique sentences
			if (!wasInserted) sentences.add(sentence)
		}

		return sentences.joinToString(separator = ",")
	}
}