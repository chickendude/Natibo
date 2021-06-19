package ch.ralena.natibo.ui.sentences.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.SentenceRoom
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import io.realm.RealmList
import javax.inject.Inject

class SentenceListAdapter @Inject constructor() :
	BaseRecyclerAdapter<SentenceListAdapter.ViewHolder, SentenceListAdapter.Listener>() {
	interface Listener {
		fun onSentenceClicked(sentence: SentenceRoom)
	}

	private lateinit var languageName: String
	private val sentences = ArrayList<SentenceRoom>()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view: View =
			LayoutInflater.from(parent.context).inflate(R.layout.item_sentence_list, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (position < itemCount) holder.bindView(sentences[position])
	}

	override fun getItemCount(): Int = sentences.size

	fun loadSentences(sentences: List<SentenceRoom>, language: LanguageRoom) {
		languageName = language.code
		this.sentences.clear()
		this.sentences.addAll(sentences)
		notifyDataSetChanged()
	}

	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val index: TextView = view.findViewById(R.id.indexLabel)
		private val languageCode: TextView = view.findViewById(R.id.languageCodeLabel)
		private val sentenceText: TextView = view.findViewById(R.id.sentenceLabel)
		private val alternateSentence: TextView = view.findViewById(R.id.alternateSentenceLabel)
		private val alternateSentenceLayout: LinearLayout =
			view.findViewById(R.id.alternateSentenceLayout)
		private val romanization: TextView = view.findViewById(R.id.romanizationLabel)
		private val romanizationLayout: LinearLayout = view.findViewById(R.id.romanizationLayout)
		private val ipa: TextView = view.findViewById(R.id.ipaLabel)
		private val ipaLayout: LinearLayout = view.findViewById(R.id.ipaLayout)
		private lateinit var sentence: SentenceRoom

		init {
			languageCode.text = languageName
			view.setOnClickListener {
				listeners.forEach { it.onSentenceClicked(sentence) }
			}
		}

		fun bindView(sentence: SentenceRoom) {
			this.sentence = sentence
			index.text = "${sentence.index}"
			sentenceText.text = sentence.original
			if (sentence.alternate.isNotEmpty()) {
				alternateSentenceLayout.visibility = View.VISIBLE
				alternateSentence.text = sentence.alternate
			} else {
				alternateSentenceLayout.visibility = View.GONE
			}
			if (sentence.romanization.isNotEmpty()) {
				romanizationLayout.visibility = View.VISIBLE
				romanization.text = sentence.romanization
			} else {
				romanizationLayout.visibility = View.GONE
			}
			if (sentence.ipa.isNotEmpty()) {
				ipaLayout.visibility = View.VISIBLE
				ipa.text = sentence.ipa
			} else {
				ipaLayout.visibility = View.GONE
			}
		}
	}
}