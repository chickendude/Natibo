package ch.ralena.natibo.model

import ch.ralena.natibo.data.room.`object`.SentenceRoom

data class NatiboSession(
	val sentences: List<NatiboSentence>,
	val sentenceIndices: List<Int>,
	var currentSentenceIndex: Int = 0,
	/** Language to play next, e.g. NTTN would play four sentences, Native/Target/Target/Native */
	var currentLanguageIndex: Int = 0
)

data class NatiboSentence(val native: SentenceRoom, val target: SentenceRoom?)
