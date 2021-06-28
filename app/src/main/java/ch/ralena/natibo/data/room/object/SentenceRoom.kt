package ch.ralena.natibo.data.room.`object`

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SentenceRoom(
	/**
	 * Sentence's index.
	 *
	 * Used to order the sentences.
	 */
	val index: Int,

	/** The original, native way of writing the sentence. */
	val original: String,

	/** Some languages, namely Japanese, have a second text format. */
	val alternate: String,

	/**
	 * Sentence's romanization.
	 *
	 * For languages written in a separate script, a romanization is provided.
	 */
	val romanization: String,

	/** International Phonetic Alphabet. */
	val ipa: String,

	/** The URI of the sentence's mp3 recording. */
	val mp3: String,

	/** The length in milliseconds of the mp3. */
	val mp3Length: Int,

	/** The pack the sentence belongs to. */
	val packId: Long,

	/** The pack the sentence belongs to. */
	val languageId: Long,

	@PrimaryKey(autoGenerate = true) val id: Long = 0
)
