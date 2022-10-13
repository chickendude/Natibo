package ch.ralena.natibo.testutils

import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.data.room.`object`.ScheduleRoom

// Schedule
val SCHEDULE = ScheduleRoom(10, 0, "01", "6432")

// Course
val COURSE = CourseRoom(
	title = "title",
	nativeLanguageId = 1,
	targetLanguageId = 2,
	schedule = SCHEDULE,
	sessionId = 1,
	packName = "F1"
)
val COURSE_ID = COURSE.id

// Pack
val PACK = PackRoom(
	"pack",
	666
)

// Language
val LANGUAGE = LanguageRoom("Basque", "eu", 666, 10)