package ch.ralena.natibo.data.room.`object`

import androidx.room.Embedded
import androidx.room.Relation

data class LanguageWithPacks(
	@Embedded val language: LanguageRoom,
	@Relation(
		parentColumn = "id",
		entityColumn = "languageId"
	)
	val packs: List<PackRoom>
)