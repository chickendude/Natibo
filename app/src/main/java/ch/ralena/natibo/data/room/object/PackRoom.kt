package ch.ralena.natibo.data.room.`object`

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
/**
 * This corresponds to a language pack, essentially a set of sentences.
 */
data class PackRoom(
	val name: String,

	/** ID of the matching [LanguageRoom] object */
	val languageCode: String,

	@PrimaryKey(autoGenerate = true) val id: Long = 0
)
