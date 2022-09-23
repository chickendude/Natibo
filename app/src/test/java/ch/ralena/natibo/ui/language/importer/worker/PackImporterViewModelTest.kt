package ch.ralena.natibo.ui.language.importer.worker

import android.content.ContentResolver
import android.net.Uri
import ch.ralena.natibo.testutils.LANGUAGE
import ch.ralena.natibo.testutils.PACK
import ch.ralena.natibo.testutils.TestDispatcherProvider
import ch.ralena.natibo.ui.language.importer.worker.listener.PackImporterListener
import ch.ralena.natibo.ui.language.importer.worker.usecase.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private val SENTENCE_STRINGS = listOf("Sentence one", "Sentence two")

@ExperimentalCoroutinesApi
internal class PackImporterViewModelTest {
	// region Helper fields-------------------------------------------------------------------------
	private val listener1 = mockk<PackImporterListener>(relaxed = true)
	private val listener2 = mockk<PackImporterListener>(relaxed = true)
	private val uri = mockk<Uri>(relaxed = true)
	private val dispatcherProvider = TestDispatcherProvider(TestCoroutineDispatcher())
	private val contentResolver = mockk<ContentResolver>(relaxed = true)
	private val countMp3sUseCase = mockk<CountMp3sUseCase>(relaxed = true)
	private val createLanguageUseCase = mockk<CreateLanguageUseCase>(relaxed = true)
	private val createPackUseCase = mockk<CreatePackUseCase>(relaxed = true)
	private val createSentencesUseCase = mockk<CreateSentencesUseCase>(relaxed = true)
	private val fetchSentencesUseCase = mockk<FetchSentencesUseCase>(relaxed = true)
	private val readPackDataUseCase = mockk<ReadPackDataUseCase>(relaxed = true)
	// endregion Helper fields----------------------------------------------------------------------

	private lateinit var SUT: PackImporterViewModel

	@BeforeEach
	fun setUp() {
		SUT = PackImporterViewModel(
			contentResolver,
			dispatcherProvider,
			countMp3sUseCase,
			createLanguageUseCase,
			createPackUseCase,
			createSentencesUseCase,
			fetchSentencesUseCase,
			readPackDataUseCase
		)
		allSuccess()
	}

	@Test
	fun `importPack creates language`() = runBlockingTest {
		// Given

		// When
		SUT.importPack(uri)

		// Then
		coVerify { createLanguageUseCase.fetchOrCreateLanguage(LANGUAGE.code) }
	}

	@Test
	fun `importPack creates pack`() = runBlockingTest {
		// Given

		// When
		SUT.importPack(uri)

		// Then
		coVerify { createPackUseCase.createPack(PACK.name, LANGUAGE.id) }
	}

	@Test
	fun `importPack creates sentences`() = runBlockingTest {
		// Given

		// When
		SUT.importPack(uri)

		// Then
		coVerify { createSentencesUseCase.createSentences(LANGUAGE.id, PACK.id, SENTENCE_STRINGS) }
	}
	// copy mp3s
	// mp3 count != sentence count show warning
	// no mp3s show warning
	// no sentences show warning

	// region Helper functions----------------------------------------------------------------------
	private fun allSuccess() {
		coEvery { readPackDataUseCase.extractLanguageAndPackName(uri) } returns Pair(
			LANGUAGE.code,
			PACK.name
		)
		coEvery { createLanguageUseCase.fetchOrCreateLanguage(any()) } returns LANGUAGE.id
		coEvery { createPackUseCase.createPack(any(), any()) } returns PACK.id
		coEvery { fetchSentencesUseCase.fetchSentences(any()) } returns SENTENCE_STRINGS
	}
	// region Helper functions----------------------------------------------------------------------
}