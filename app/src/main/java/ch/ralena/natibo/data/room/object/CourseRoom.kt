package ch.ralena.natibo.data.room.`object`

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourseRoom(
	/** Title of the course, e.g. "English -> Cantonese". */
	val title: String,

	/** Abbreviation of the base language. Generally the user's native language. */
	val baseLanguageCode: String,

	/** Abbreviation of the target language. The language the user is studying. */
	val targetLanguageCode: String,

	/** Contains information on the scheduling of the course. */
	@Embedded
	val schedule: ScheduleRoom,

	/** The ID of the current session. */
	val sessionId: Long,

	/** The amount of time to pause between sentences. Measured in milliseconds. */
	val pause: Int = 1000,

	/** How fast to play sentence audio. Normal playback is `1`. */
	val playbackSpeed: Float = 1f,

	/** Total number of reps that have been done. */
	val repCount: Int = 0,

	@PrimaryKey(autoGenerate = true) val id: Long = 0
)

data class ScheduleRoom(
	/** How many new sentences to learn each day. */
	val numSentences: Int,

	/** The index of the first new sentence. */
	val curSentenceIndex: Int,

	/** The order to play sentences in. E.g. "Base Target Target" or "Target Base Target" */
	val order: String,

	/**
	 * How many times to see each sentence.
	 *
	 * For example: 6, 4, 3, 2
	 *  1. Day 1: See sentence 6 times.
	 *  2. Day 2: See sentence 4 times.
	 *  3. Day 3: See sentence 3 times.
	 *  4. Day 4: See sentence 2 times.
	 *
	 *  Remember that previous sentences will be added, so if you have 10 new sentences a day, on
	 *  day 1 you will see 60 sentences. On day 2, you will see day 1's sentences 40 times and a new
	 *  set of sentences 60 times. On day 3, you will see day 1's sentences 30 times, day 2's
	 *  sentences 40 times, and 60 new sentences, etc.
	 */
	val reviewPattern: String
)
