package ch.ralena.natibo.data.room

import ch.ralena.natibo.R
import ch.ralena.natibo.testutils.*
import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.data.room.dao.CourseDao
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import io.realm.Realm
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// region Constants---------------------------------------------------------------------------------

// endregion Constants------------------------------------------------------------------------------

@ExperimentalCoroutinesApi
internal class CourseRepositoryTest {
	// region Helper fields-------------------------------------------------------------------------
	private val realm = mockk<Realm>(relaxed = true)
	private val courseDao = mockk<CourseDao>(relaxed = true)
	// endregion Helper fields----------------------------------------------------------------------

	private lateinit var sut: CourseRepository

	@BeforeEach
	fun setUp() {
		sut = CourseRepository(realm, courseDao)
	}

	@Test
	fun `fetchCourse success notifies of success with correct data`() = runBlockingTest {
		// Given
		success()

		// When
		val result = sut.fetchCourse(COURSE_ID)

		// Then
		result.shouldBeInstanceOf<Result.Success<CourseRoom>>()
		result.data.id.shouldBe(COURSE_ID)
	}

	@Test
	fun `fetchCourse not found notifies of failure`() = runBlockingTest {
		// Given
		failure()

		// When
		val result = sut.fetchCourse(COURSE_ID)

		// Then
		result.shouldBeInstanceOf<Result.Failure<Any>>()
		result.stringRes.shouldBe(R.string.course_not_found)
	}

	@Test
	fun `fetchCourses queries courseDao`() = runBlockingTest {
		// Given

		// When
		sut.fetchCourses()

		// Then
		coVerify { courseDao.getAll() }
	}

	// region Helper methods------------------------------------------------------------------------
	private fun success() {
		coEvery { courseDao.getCourseById(COURSE_ID) } returns COURSE
	}

	private fun failure() {
		coEvery { courseDao.getCourseById(COURSE_ID) } returns null
	}
	// endregion Helper methods---------------------------------------------------------------------
}