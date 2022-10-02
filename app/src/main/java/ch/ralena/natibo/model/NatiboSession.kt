package ch.ralena.natibo.model

import ch.ralena.natibo.data.room.`object`.SentenceRoom
import java.lang.Integer.max

data class NatiboSession(
	val sentences: List<NatiboSentence>,
	val sentenceIndices: List<Int>,
	val sessionId: Long,
	val languageOrder: List<Int>,
	var currentSentenceIndex: Int = -1,
	/** Language to play next, e.g. 0110 would play four sentences, Native/Target/Target/Native */
	var currentLanguageIndex: Int = -1
) {
	val currentSentencePair: NatiboSentence?
		get() = sentenceIndices
			.getOrNull(currentSentenceIndex)
			?.let { index -> sentences.find { it.native.index == index } }

	val currentSentence: SentenceRoom?
		get() = if (languageOrder[currentLanguageIndex] == 0) currentSentencePair?.native
		else currentSentencePair?.target

	init {
		if (currentSentenceIndex < 0) currentSentenceIndex = 0
		if (currentLanguageIndex < 0) currentLanguageIndex = 0
	}

	fun nextSentence() {
		currentLanguageIndex += 1
		if (currentLanguageIndex >= languageOrder.size || currentSentenceIndex < 0) {
			currentLanguageIndex = 0
			currentSentenceIndex++
		}
	}

	fun nextSentencePair() {
		currentSentenceIndex++
		currentLanguageIndex = 0
	}

	fun previousSentencePair() {
		currentSentenceIndex = max(0, currentSentenceIndex - 1)
		currentLanguageIndex = 0
	}
}

data class NatiboSentence(val native: SentenceRoom, val target: SentenceRoom?)
