package ch.ralena.natibo.usecases.data

import ch.ralena.natibo.data.NatiboResult
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.SentenceRepository
import ch.ralena.natibo.data.room.SessionRepository
import ch.ralena.natibo.model.NatiboSentence
import ch.ralena.natibo.model.NatiboSession
import javax.inject.Inject

class FetchSessionWithSentencesUseCase @Inject constructor(
	private val courseRepository: CourseRepository,
	private val sessionRepository: SessionRepository,
	private val sentenceRepository: SentenceRepository
) {
	suspend fun fetchSessionWithSentences(sessionId: Long): NatiboSession? {
		val session = sessionRepository.fetchSession(sessionId) ?: return null
		val result = courseRepository.fetchCourse(session.courseId)
		if (result !is NatiboResult.Success) return null

		val course = result.data
		val startingIndex = course.schedule.curSentenceIndex
		val endingIndex = course.schedule.curSentenceIndex + course.schedule.numSentences
		val languageOrder = course.schedule.order.toCharArray().map { it.digitToInt() }
		val targetSentences = if (course.targetLanguageId != null) {
			sentenceRepository.fetchSentencesInPack(
				packId = course.packId,
				languageId = course.targetLanguageId,
				start = startingIndex,
				end = endingIndex
			)
		} else listOf()
		val nativeSentences =
			sentenceRepository.fetchSentencesInPack(
				packId = course.packId,
				languageId = course.nativeLanguageId,
				start = startingIndex,
				end = endingIndex
			)
		val indices = session.sentenceIndices.split(",").map { it.toInt() }
		// If native = null, skip the sentence. If target == null, only skip it if it's not supposed
		// to be null
		val sentences = mutableListOf<NatiboSentence>()
		for (i in nativeSentences.indices) {
			val native = nativeSentences.getOrNull(i) ?: continue
			val target = targetSentences.getOrNull(i)
			if (course.targetLanguageId != null && target == null) continue
			sentences.add(NatiboSentence(native, target))
		}
		return NatiboSession(
			sentences,
			sentenceIndices = indices,
			languageOrder = languageOrder,
			sessionId = sessionId
		)
	}
}