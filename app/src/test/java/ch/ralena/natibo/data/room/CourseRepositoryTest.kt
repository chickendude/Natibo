package ch.ralena.natibo.data.room

import ch.ralena.natibo.data.MyResult
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.ui.course.detail.CourseDetailViewModel
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.realm.Realm
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CourseRepositoryTest {
	// region Constants-----------------------------------------------------------------------------
	companion object {
		private val COURSE = Course()
		private val COURSE_ID = COURSE.id
	}
	// endregion Constants--------------------------------------------------------------------------

	// region Helper fields-------------------------------------------------------------------------
	private lateinit var sut: CourseRepository
	private val listener = mockk<(MyResult) -> Unit>(relaxed = true)
	private val realm = mockk<Realm>()
	// endregion Helper fields----------------------------------------------------------------------

	@BeforeEach
	fun setUp() {
		sut = CourseRepository(realm)
	}

	@Test
	fun `fetchCourse success notifies of success with correct data`() {
		// Given
		success()

		// When
		sut.fetchCourse(COURSE_ID, listener)

		// Then
		verify { listener(MyResult.Success(COURSE)) }
	}

	@Test
	fun `fetchCourse not found notifies of failure`() {
		// Given
		nullResult()

		// When
		sut.fetchCourse(COURSE_ID, listener)

		// Then
		verify { listener(MyResult.Failure) }
	}

	// region Helper methods------------------------------------------------------------------------
	private fun success() {
		every {
			realm.where(Course::class.java).equalTo("id", COURSE_ID).findFirst()
		} returns COURSE
	}

	private fun nullResult() {
		every { realm.where(Course::class.java).equalTo("id", COURSE_ID).findFirst() } returns null
	}
	// endregion Helper methods---------------------------------------------------------------------

	// region Helper classes------------------------------------------------------------------------
	// endregion Helper classes---------------------------------------------------------------------
}