package ch.ralena.natibo.testutils

import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.ScheduleRoom

// Schedule
val SCHEDULE = ScheduleRoom(10, 0, "01", "6432")

// Course
val COURSE = CourseRoom(
	title = "title",
	baseLanguageCode = "en",
	targetLanguageCode = "es",
	schedule = SCHEDULE,
	session = null
)
val COURSE_ID = COURSE.id