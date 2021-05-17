package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.Course
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.ui.course.detail.CourseDetailViewModel.*
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.realm.RealmList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CourseDetailViewModelTest {
	// region Constants-----------------------------------------------------------------------------
	companion object {
		private var COURSE = Course()
		private val COURSE_ID = COURSE.id
		const val PACK_ID = "packId"
	}
	// endregion Constants--------------------------------------------------------------------------

	// region Helper fields-------------------------------------------------------------------------
	private lateinit var sut: CourseDetailViewModel
	private val listener1 = mockk<Listener>(relaxed = true)
	private val listener2 = mockk<Listener>(relaxed = true)
	private val courseRepository = mockk<CourseRepository>(relaxed = true)
	private lateinit var pack: Pack
	// endregion Helper fields----------------------------------------------------------------------

	@BeforeEach
	fun setUp() {
		pack = Pack()
		COURSE.packs = RealmList()
		sut = CourseDetailViewModel(courseRepository)
		listenersRegistered()
	}

	@Test
	fun `fetchCourse null notifies course not found`() {
		// Given

		// When
		sut.fetchCourse(null)

		// Then
		verify { listener1.onCourseNotFound() }
	}

	@Test
	fun `fetchCourse success notifies listeners with correct data`() {
		// Given
		success()

		// When
		sut.fetchCourse(COURSE_ID)

		// Then
		verify { listener1.onCourseFetched(COURSE) }
		verify { listener2.onCourseFetched(COURSE) }
	}

	@Test
	fun `fetchCourse failure notifies course not found`() {
		// Given
		failure()

		// When
		sut.fetchCourse(COURSE_ID)

		// Then
		verify { listener1.onCourseNotFound() }
		verify { listener2.onCourseNotFound() }
	}

	@Test
	fun `addRemovePack calls courserepository toggle`() = runBlocking {
		// Given
		sut.courseId = COURSE_ID

		// When
		sut.addRemovePack(PACK_ID)

		// Then
		coVerify { courseRepository.togglePackInCourse(PACK_ID, COURSE_ID) }
	}

	// region Helper methods------------------------------------------------------------------------
	private fun success() {
		every { courseRepository.fetchCourse(COURSE_ID, any()) } answers {
			val callback = this.args[1] as (result: Result<Course>) -> Unit
			callback(Result.Success(COURSE))
		}
	}

	private fun failure() {
		every { courseRepository.fetchCourse(COURSE_ID, any()) } answers {
			val callback = this.args[1] as (result: Result<Course>) -> Unit
			callback(Result.Failure(null))
		}
	}

	private fun listenersRegistered() {
		sut.registerListener(listener1)
		sut.registerListener(listener2)
	}
	// endregion Helper methods---------------------------------------------------------------------

	// region Helper classes------------------------------------------------------------------------
	// endregion Helper classes---------------------------------------------------------------------
}