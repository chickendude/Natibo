package ch.ralena.natibo.ui.language.importer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.*
import ch.ralena.natibo.R
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.language.importer.worker.PackImporterWorker
import ch.ralena.natibo.ui.language.list.LanguageListFragment
import ch.ralena.natibo.utils.GLSImporter

class LanguageImportFragment : Fragment() {

	var progressBar: ProgressBar? = null
	var fileNameText: TextView? = null
	var actionText: TextView? = null
	var counterText: TextView? = null
	var totalText: TextView? = null
	var dividerBarLabel: TextView? = null
	var curAction = 0

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_language_import, container, false)
		progressBar = view.findViewById(R.id.progressBar)
		fileNameText = view.findViewById(R.id.fileNameText)
		actionText = view.findViewById(R.id.actionText)
		counterText = view.findViewById(R.id.counterText)
		totalText = view.findViewById(R.id.totalText)
		dividerBarLabel = view.findViewById(R.id.dividerBarLabel)
		return view
	}

	@SuppressLint("CheckResult")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
			.observe(viewLifecycleOwner, {
				if (it != null) {
					val progress = it.progress
					val value = progress.getInt("int", 0)
				}
			})
		workManager
			.enqueue(workRequest)
		return
		val importer = GLSImporter()

		// the total counter
		importer.totalObservable().subscribe { total: Int ->
			if (activity != null) requireActivity().runOnUiThread {
				totalText!!.text = total.toString()
				progressBar!!.max = total
			}
		}

		// the progress counter
		importer.progressObservable().subscribe { progress: Int ->
			if (activity != null) requireActivity().runOnUiThread {
				counterText!!.text = progress.toString()
				progressBar!!.progress = progress
				// if pack has finished loading, go to the language list screen.
				if (curAction == ACTION_EXTRACTING_AUDIO && progressBar!!.max == progress) {
					loadLanguageListFragment()
				}
			}
		}

		// load filename
		importer.fileNameSubject().subscribe { filename: String? ->
			if (activity != null) requireActivity().runOnUiThread {
				fileNameText!!.visibility = View.VISIBLE
				fileNameText!!.text = filename
			}
		}

		// the currently happening action
		importer.actionSubject().subscribe { actionId: Int ->
			if (activity != null) requireActivity().runOnUiThread {
				curAction = actionId
				when (actionId) {
					ACTION_OPENING_FILE -> openFile()
					ACTION_COUNTING_SENTENCES -> countSentences()
					ACTION_READING_SENTENCES -> readSentences()
					ACTION_EXTRACTING_TEXT -> extractText()
					ACTION_EXTRACTING_AUDIO -> extractAudio()
					ACTION_EXIT -> loadLanguageListFragment()
				}
			}
		}
		importer.importPack(activity as MainActivity?, uri)
	}

	private fun extractAudio() {
		actionText!!.text = resources.getString(R.string.extracting_sentence_audio)
	}

	private fun readSentences() {
		actionText!!.text = resources.getString(R.string.reading_sentences)
		counterText!!.visibility = View.GONE
		dividerBarLabel!!.visibility = View.GONE
		totalText!!.visibility = View.GONE
	}

	private fun extractText() {
		actionText!!.text = resources.getString(R.string.extracting_sentence_text)
		counterText!!.visibility = View.VISIBLE
		totalText!!.visibility = View.VISIBLE
		dividerBarLabel!!.visibility = View.VISIBLE
	}

	private fun countSentences() {
		actionText!!.text = resources.getString(R.string.counting_sentences)
		counterText!!.visibility = View.VISIBLE
		totalText!!.visibility = View.VISIBLE
		dividerBarLabel!!.visibility = View.VISIBLE
	}

	private fun openFile() {
		actionText!!.text = resources.getString(R.string.opening_file)
		fileNameText!!.visibility = View.VISIBLE
		actionText!!.visibility = View.VISIBLE
		counterText!!.visibility = View.GONE
		totalText!!.visibility = View.GONE
		dividerBarLabel!!.visibility = View.GONE
	}

	private fun loadLanguageListFragment() {
		val fragment = LanguageListFragment()
		parentFragmentManager.beginTransaction()
			.replace(R.id.fragmentPlaceHolder, fragment)
			.commit()
	}

	companion object {
		const val EXTRA_URI = "extra_uri"
		const val ACTION_OPENING_FILE = 0
		const val ACTION_COUNTING_SENTENCES = 1
		const val ACTION_READING_SENTENCES = 2
		const val ACTION_EXTRACTING_TEXT = 3
		const val ACTION_EXTRACTING_AUDIO = 4
		const val ACTION_EXIT = 5
	}
}