package ch.ralena.natibo.settings

import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.settings.types.IntSetting
import ch.ralena.natibo.utils.StorageManager
import javax.inject.Inject

class CourseSettings @Inject constructor(private val storageManager: StorageManager) {
	lateinit var course: CourseRoom

	val delayBetweenSentences: IntSetting
		get() = IntSetting(
			"course_${course.id}_delay_between_sentences",
			R.string.settings_course_sentence_delay_title,
			R.string.settings_course_sentence_delay_description,
			storageManager,
			1000
		)
}
