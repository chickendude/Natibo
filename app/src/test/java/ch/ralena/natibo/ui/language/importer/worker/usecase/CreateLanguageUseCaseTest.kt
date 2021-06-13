package ch.ralena.natibo.ui.language.importer.worker.usecase

import ch.ralena.natibo.data.room.LanguageRepository
import ch.ralena.natibo.testutils.*
import ch.ralena.natibo.ui.language.importer.worker.ImportException
import io.kotest.assertions.throwables.shouldThrow
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
	fun `createLanguage fail throws exception`() = runBlockingTest {
		// Given
		failure()

		// When
		shouldThrow<ImportException> {
			SUT.createLanguage(LANGUAGE.code)
		}

		// Then
	}

	@Test
	fun `createLanguage creates language`() = runBlockingTest {
	    // Given
	    success()

	    // When
	    SUT.createLanguage(LANGUAGE.code)

	    // Then
	    coVerify { languageRepository.createLanguage(LANGUAGE.code) }
	}

	// region Helper functions----------------------------------------------------------------------
	private fun success() {
		coEvery { languageRepository.createLanguage(LANGUAGE.code) } returns LANGUAGE.id
	}

	private fun failure() {
		coEvery { languageRepository.createLanguage(LANGUAGE.code) } returns null
	}
	// endregion Helper functions-------------------------------------------------------------------
}