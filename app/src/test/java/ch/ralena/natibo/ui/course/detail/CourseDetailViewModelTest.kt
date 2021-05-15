package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.ui.course.detail.CourseDetailViewModel.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.realm.Realm
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CourseDetailViewModelTest {
	// region Constants-----------------------------------------------------------------------------
	companion object {
		private val COURSE = Course()
		private val COURSE_ID = COURSE.id
	}
	// endregion Constants--------------------------------------------------------------------------

	// region Helper fields-------------------------------------------------------------------------
	private lateinit var sut: CourseDetailViewModel
	private val listener1 = mockk<Listener>(relaxed = true)
	private val listener2 = mockk<Listener>(relaxed = true)
	private val courseRepository = mockk<CourseRepository>()
	// endregion Helper fields----------------------------------------------------------------------

	@BeforeEach
	fun setUp() {
		sut = CourseDetailViewModel(courseRepository)
	}

	// fetchCourse success notifies listeners with correct data

	@Test
	fun `fetchCourse success notifies listeners with correct data`() {
		// Given
		every { courseRepository.fetchCourse(COURSE_ID, any()) } answers {
			val callback = this.args[1] as (result: Result<Course>) -> Unit
			callback(Result.Success(COURSE))
		}

		// When
		sut.registerListener(listener1)
		sut.registerListener(listener2)
		sut.fetchCourse(COURSE_ID)

		// Then
		verify { listener1.onCourseFetched(COURSE) }
		verify { listener2.onCourseFetched(COURSE) }
	}
	// fetchCourse failure notifies listeners of failure

	// region Helper methods------------------------------------------------------------------------
	// endregion Helper methods---------------------------------------------------------------------

	// region Helper classes------------------------------------------------------------------------
	// endregion Helper classes---------------------------------------------------------------------
}