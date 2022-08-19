package ch.ralena.natibo.ui.sentences

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.PackWithSentences
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.databinding.FragmentSentenceListBinding
import ch.ralena.natibo.ui.MainActivity
import ch.ralena.natibo.ui.base.BaseFragment
import ch.ralena.natibo.ui.sentences.adapter.SentenceListAdapter
import ch.ralena.natibo.ui.sentences.listener.SentenceSeekBarChangeListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SentenceListFragment :
	BaseFragment<FragmentSentenceListBinding, SentenceListViewModel.Listener, SentenceListViewModel>(
		FragmentSentenceListBinding::inflate
	), SentenceListViewModel.Listener,
	SentenceSeekBarChangeListener.Listener,
	SentenceListAdapter.Listener {

	companion object {
		val TAG: String = SentenceListFragment::class.java.simpleName
		const val TAG_LANGUAGE_ID = "language_id"
		const val TAG_PACK_ID = "pack_id"
	}

	@Inject
	lateinit var mainActivity: MainActivity

	@Inject
	lateinit var seekBarChangeListener: SentenceSeekBarChangeListener

	@Inject
	lateinit var sentenceListAdapter: SentenceListAdapter

	override fun setupViews(view: View) {
		viewModel.registerListener(this)
		sentenceListAdapter.registerListener(this)

		// load language and pack from database
		val languageId = requireArguments().getLong(TAG_LANGUAGE_ID)
		val basePackId = requireArguments().getLong(TAG_PACK_ID)
		viewModel.fetchInfo(languageId, basePackId)

		// prepare seekbar
		seekBarChangeListener.registerListener(this)
		binding.seekbar.apply {
			setOnSeekBarChangeListener(seekBarChangeListener)
		}

		// set up RecyclerView
		binding.recyclerView.apply {
			adapter = sentenceListAdapter
			layoutManager = LinearLayoutManager(context)
			addOnScrollListener(object : RecyclerView.OnScrollListener() {
				override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
					super.onScrolled(recyclerView, dx, dy)
					binding.seekbar.apply {
						setOnSeekBarChangeListener(null)
						progress =
							(binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
						setOnSeekBarChangeListener(seekBarChangeListener)
					}
				}
			})
		}
	}

	// region Listener overrides -------------------------------------------------------------------
	override fun onProgressChanged(progress: Int) {
		binding.recyclerView.scrollToPosition(progress)
	}

	override fun onInfoFetched(language: LanguageRoom, sentences: List<SentenceRoom>) {
		// load language name
		mainActivity.title = language.name
		binding.seekbar.max = sentences.size
		sentenceListAdapter.loadSentences(sentences, language)
	}

	override fun onError() {
		Toast.makeText(context, "Error loading sentences", Toast.LENGTH_SHORT).show()
	}

	override fun onSentenceClicked(sentence: SentenceRoom) {
		viewModel.playSentence(sentence)
	}
	// endregion Listener overrides ----------------------------------------------------------------
}