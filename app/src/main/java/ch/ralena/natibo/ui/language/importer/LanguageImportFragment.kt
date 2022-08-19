package ch.ralena.natibo.ui.language.importer

import android.net.Uri
import android.view.View
import androidx.work.*
import androidx.work.WorkManager
import ch.ralena.natibo.databinding.FragmentLanguageImportBinding
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import dagger.hilt.android.AndroidEntryPoint

enum class ImportProgress {
	ACTION_TEXT,
	ACTION_PROGRESS,
	ACTION_COMPLETED
}

@AndroidEntryPoint
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
		const val WORKER_MESSAGE = "worker_message"
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

	// region Helper functions----------------------------------------------------------------------
	private fun launchWorker() {
		val uri = requireArguments().getParcelable<Uri>(EXTRA_URI) ?: return
		requireArguments().remove(EXTRA_URI)

		val data = Data.Builder()
			.putString("uri", uri.toString())
			.build()

		val workRequest = OneTimeWorkRequestBuilder<PackImporterWorker>()
			.setInputData(data)
			.build()
		val workManager = WorkManager.getInstance(requireContext())
		workManager
			.getWorkInfoByIdLiveData(workRequest.id)
			.observe(viewLifecycleOwner) { workInfo ->
				if (workInfo != null) {
					val progress = workInfo.progress
					val updateType = progress.getInt(WORKER_ACTION, -1)
					when (updateType) {
						ImportProgress.ACTION_TEXT.ordinal ->
							binding.actionText.text = progress.getString(WORKER_MESSAGE)
						ImportProgress.ACTION_PROGRESS.ordinal ->
							binding.progressBar.progress = progress.getInt(WORKER_PROGRESS, 0)
						ImportProgress.ACTION_COMPLETED.ordinal -> {
							viewModel.workComplete()
						}
					}
				}
			}
		workManager.enqueue(workRequest)
	}
	// endregion Helper functions-------------------------------------------------------------------
}