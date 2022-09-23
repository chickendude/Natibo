package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.testutils.LANGUAGE
import ch.ralena.natibo.ui.language.importer.worker.ImportException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class CreateLanguageUseCaseTest {
	// region Helper fields-------------------------------------------------------------------------
	private val languageRepository = mockk<LanguageRepository>(relaxed = true)
	// endregion Helper fields----------------------------------------------------------------------

	private lateinit var SUT: CreateLanguageUseCase

	@BeforeEach
	fun setUp() {
		SUT = CreateLanguageUseCase(languageRepository)
	}

	@Test
	fun `fetchOrCreateLanguage failing to create throws exception`() = runBlockingTest {
		// Given
		languageNotFound()
		createFailure()

		// When
		shouldThrow<ImportException> {
			SUT.fetchOrCreateLanguage(LANGUAGE.code)
		}

		// Then
	}

	@Test
	fun `fetchOrCreateLanguage creates new language`() = runBlockingTest {
		// Given
		languageNotFound()
		createSuccess()

		// When
		val result = SUT.fetchOrCreateLanguage(LANGUAGE.code)

		// Then
		coVerify { languageRepository.createLanguage(LANGUAGE.code) }
		result.shouldBe(LANGUAGE.id)
	}

	@Test
	fun `fetchOrCreateLanguage returns existing language`() = runBlockingTest {
		// Given
		languageFound()

		// When
		val result = SUT.fetchOrCreateLanguage(LANGUAGE.code)

		// Then
		result.shouldBe(LANGUAGE.id)
		coVerify(exactly = 0) { languageRepository.createLanguage(any()) }
	}

	// region Helper functions----------------------------------------------------------------------
	private fun languageFound() {
		coEvery { languageRepository.fetchByCode(LANGUAGE.code) } returns LANGUAGE
	}

	private fun languageNotFound() {
		coEvery { languageRepository.fetchByCode(LANGUAGE.code) } returns null
	}

	private fun createSuccess() {
		coEvery { languageRepository.createLanguage(LANGUAGE.code) } returns LANGUAGE.id
	}

	private fun createFailure() {
		coEvery { languageRepository.createLanguage(LANGUAGE.code) } returns null
	}
	// endregion Helper functions-------------------------------------------------------------------
}