package ch.ralena.natibo.data.room.`object`

import androidx.room.Embedded
import androidx.room.Relation

data class PackWithSentences(
	@Embedded val pack: PackRoom,
	@Relation(
		parentColumn = "id",
		entityColumn = "packId"
	)
	val sentences: List<SentenceRoom>
)