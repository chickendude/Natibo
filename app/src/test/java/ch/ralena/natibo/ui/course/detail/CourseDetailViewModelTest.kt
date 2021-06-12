package ch.ralena.natibo.ui.course.detail

import ch.ralena.natibo.data.Result
import ch.ralena.natibo.data.room.CourseRepository
import ch.ralena.natibo.data.room.`object`.CourseRoom
import ch.ralena.natibo.testutils.*
import ch.ralena.natibo.ui.course.detail.CourseDetailViewModel.*
import ch.ralena.natibo.utils.ScreenNavigator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class CourseDetailViewModelTest {
	// region Helper fields-------------------------------------------------------------------------
	private val listener1 = mockk<Listener>(relaxed = true)
	private val listener2 = mockk<Listener>(relaxed = true)
	private val courseRepository = mockk<CourseRepository>(relaxed = true)
	private val screenNavigator = mockk<ScreenNavigator>(relaxed = true)
	private val testDispatcher = TestCoroutineDispatcher()
	private val testDispatcherProvider = TestDispatcherProvider(testDispatcher)
	// endregion Helper fields----------------------------------------------------------------------

	private lateinit var SUT: CourseDetailViewModel

	@BeforeEach
	fun setUp() {
		Dispatchers.setMain(testDispatcher)
		SUT = CourseDetailViewModel(courseRepository, screenNavigator, testDispatcherProvider)
		listenersRegistered()
	}

	@AfterEach
	fun tearDown() {
		Dispatchers.resetMain()
		unregisterListeners()
	}

	@Test
	fun `fetchCourse invalid ID notifies course not found`() {
		// Given
		fetchCourseFailure()

		// When
		SUT.fetchCourse(COURSE_ID)

		// Then
		verify { listener1.onCourseNotFound() }
		verify { listener2.onCourseNotFound() }
	}

	@Test
	fun `fetchCourse success notifies listeners with correct data`() {
		// Given
		fetchCourseSuccess()

		// When
		SUT.fetchCourse(COURSE_ID)

		// Then
		verify { listener1.onCourseFetched(COURSE) }
		verify { listener2.onCourseFetched(COURSE) }
	}

	// startSession no course notifies course start error
	// startSession no packs selected notifies of error
	// startSession success navigates to study session fragment
	@Test
	fun `startSession success navigates to study session fragment`() {
		// Given
		fetchCourseSuccess()
		SUT.fetchCourse(COURSE_ID)

		// When
		SUT.startSession()

		// Then
		verify { screenNavigator.toStudySessionFragment(any()) }
	}

	// deleteCourse no course notifies of error and does not navigate
	// deleteCourse success deletes course and navigates to course list fragment
	@Test
	fun `deleteCourse with course loaded deletes course and navigates to course list fragment`() {
		// Given
		fetchCourseSuccess()
		SUT.fetchCourse(COURSE_ID)

		// When
		SUT.deleteCourse()

		// Then
		coVerify { courseRepository.deleteCourse(COURSE) }
	}

	// openSettings no course notifies of error
	// openSettings success navigates to course settings fragment
	@Test
	fun `openSettings with course loaded navigates to course settings fragment`() {
		// Given
		fetchCourseSuccess()
		SUT.fetchCourse(COURSE_ID)

		// When
		SUT.openSettings()

		// Then
		verify { screenNavigator.toCourseSettingsFragment(COURSE_ID) }
	}

	// region Helper methods------------------------------------------------------------------------
	private fun fetchCourseSuccess() {
		val success = Result.Success(COURSE)
		coEvery { courseRepository.fetchCourse(COURSE_ID) } returns success
	}

	private fun fetchCourseFailure() {
		val failure = mockk<Result.Failure<CourseRoom>>()
		coEvery { courseRepository.fetchCourse(COURSE_ID) } returns failure
	}

	private fun listenersRegistered() {
		SUT.registerListener(listener1)
		SUT.registerListener(listener2)
	}

	private fun unregisterListeners() {
		SUT.unregisterListener(listener1)
		SUT.unregisterListener(listener2)
	}
	// endregion Helper methods---------------------------------------------------------------------

	// region Helper classes------------------------------------------------------------------------
	// endregion Helper classes---------------------------------------------------------------------
}