package ch.ralena.natibo.data.room.`object`

import androidx.room.*

/**
 * A language.
 *
 * All sentences are organized into packs, and each pack is tied to a language.
 */
@Entity
data class LanguageRoom (
	val name: String,
	val code: String,
	val flagDrawable: Int,
	@PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Entity(primaryKeys = ["languageId", "packId"])
data class LanguagePackCrossRef(
	val languageId: Long,
	val packId: Long
)

data class LanguageWithPacks(
	@Embedded val language: LanguageRoom,
	@Relation(
		parentColumn = "id",
		entityColumn = "id",
		associateBy = Junction(
			LanguagePackCrossRef::class,
			parentColumn = "languageId",
			entityColumn = "packId"
		)
	)
	val packs: List<PackRoom>
)