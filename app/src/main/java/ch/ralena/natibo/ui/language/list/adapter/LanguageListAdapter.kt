package ch.ralena.natibo.ui.language.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.data.room.`object`.LanguageWithPacks
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import java.util.*
import javax.inject.Inject

class LanguageListAdapter @Inject constructor(
		private val languages: ArrayList<LanguageWithPacks>
) : BaseRecyclerAdapter<LanguageListAdapter.ViewHolder, LanguageListAdapter.Listener>() {
	interface Listener {
		fun onLanguageClicked(language: LanguageRoom)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_language_list, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (position < itemCount) holder.bindView(languages[position])
	}

	override fun getItemCount(): Int {
		return languages.size
	}

	fun loadLanguages(languages: List<LanguageWithPacks>) {
		this.languages.clear()
		this.languages.addAll(languages)
		notifyDataSetChanged()
	}

	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val languageName: TextView = view.findViewById(R.id.languageLabel)
		private val numPacks: TextView = view.findViewById(R.id.numPacksLabel)
		private val numSentences: TextView = view.findViewById(R.id.numSentencesLabel)
		private val flagImage: ImageView = view.findViewById(R.id.flag_image)

		private lateinit var languageWithPacks: LanguageWithPacks

		init {
			view.setOnClickListener { for (l in listeners) l.onLanguageClicked(languageWithPacks.language) }
		}

		fun bindView(languageWithPacks: LanguageWithPacks) {
			this.languageWithPacks = languageWithPacks
			val language = languageWithPacks.language
			val packs = languageWithPacks.packs
			languageName.text = language.name
			numPacks.text = "${packs.size}"
//			numSentences.text = "${language.sentenceCount}"
			flagImage.setImageResource(language.flagDrawable)
		}
	}
}