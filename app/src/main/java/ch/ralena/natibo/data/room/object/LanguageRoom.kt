package ch.ralena.natibo.data.room.`object`

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
/**
 * A language.
 *
 * All sentences are organized into packs, and each pack is tied to a language.
 */
data class LanguageRoom (
	val name: String,
	val code: String,
	val flagDrawable: Int,
	@PrimaryKey(autoGenerate = true) val id: Long = 0
)