package ch.ralena.natibo.data.room.`object`

import androidx.room.*

@Entity
data class SessionRoom(
	/** Index of the session within the course. */
	val index: Int,

	/** Progress within the session. Essentially, the index of the current sentence. */
	val progress: Int,

	/** ID of the course the session belongs to. */
	val courseId: Long,

	/** Indices of the sentences to be studied, comma-separated. */
	val sentenceIndices: String,

	@PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Entity(primaryKeys = ["sessionId", "sentenceId"])
data class SessionSentenceCrossRef(
	val sessionId: Long,
	val sentenceId: Long
)

data class SessionWithSentences(
	@Embedded val session: SessionRoom,
	@Relation(
		parentColumn = "id",
		entityColumn = "id",
		associateBy = Junction(
			SessionSentenceCrossRef::class,
			parentColumn = "sessionId",
			entityColumn = "sentenceId"
		)
	)
	val sentences: List<SentenceRoom>
)
