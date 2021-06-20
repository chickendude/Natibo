package ch.ralena.natibo.ui.language.importer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.work.*
import androidx.work.WorkManager
import ch.ralena.natibo.R
import ch.ralena.natibo.databinding.FragmentLanguageImportBinding
import ch.ralena.natibo.di.component.PresentationComponent
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import ch.ralena.natibo.ui.language.list.LanguageListFragment

enum class ImportProgress {
	ACTION_TEXT,
	ACTION_PROGRESS,
	SENTENCES_LOADED
}
class LanguageImportFragment :
	BaseFragment<
			FragmentLanguageImportBinding,
			LanguageImportViewModel.Listener,
			LanguageImportViewModel>(
		FragmentLanguageImportBinding::inflate
	) {
	companion object {
		const val EXTRA_URI = "extra_uri"
		const val WORKER_ACTION = "worker_action"
		const val WORKER_VALUE = "worker_value"
		const val WORKER_PROGRESS = "worker_progress"
		const val ACTION_OPENING_FILE = 0
		const val ACTION_COUNTING_SENTENCES = 1
		const val ACTION_READING_SENTENCES = 2
		const val ACTION_EXTRACTING_TEXT = 3
		const val ACTION_EXTRACTING_AUDIO = 4
		const val ACTION_EXIT = 5
	}

	var curAction = 0


	override fun setupViews(view: View) {
		launchWorker()
	}

	override fun injectDependencies(injector: PresentationComponent) {
		injector.inject(this)
	}

	// region Helper functions----------------------------------------------------------------------
	private fun launchWorker() {
		val uri = requireArguments().getParcelable<Uri>(EXTRA_URI)

		val data = Data.Builder().apply {
			putString("uri", uri.toString())
		}.build()

		val workRequest = OneTimeWorkRequestBuilder<PackImporterWorker>()
			.setInputData(data)
			.build()
		val workManager = WorkManager.getInstance(requireContext())
		workManager
			.getWorkInfoByIdLiveData(workRequest.id)
			.observe(viewLifecycleOwner, { workInfo ->
				if (workInfo != null) {
					val progress = workInfo.progress
					val updateType = progress.getInt(WORKER_ACTION, -1)
					when(updateType) {
						ImportProgress.ACTION_TEXT.ordinal -> {
							val message = progress.getString(WORKER_VALUE)
							binding.actionText.text = message
						}
						ImportProgress.ACTION_PROGRESS.ordinal -> {
							binding.progressBar.progress = progress.getInt(WORKER_PROGRESS, 0)
						}
						ImportProgress.SENTENCES_LOADED.ordinal -> {
							binding.actionText.text = "Sentences loaded"
						}
					}
				}
			})
		workManager.enqueue(workRequest)
	}
	// endregion Helper functions-------------------------------------------------------------------
}