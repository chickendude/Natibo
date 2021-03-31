package ch.ralena.natibo.ui.course.create.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AvailableLanguagesAdapter @Inject constructor(
		private val languages: ArrayList<Language>,
		private val selectedLanguages: ArrayList<Language>
) : BaseRecyclerAdapter<AvailableLanguagesAdapter.ViewHolder, AvailableLanguagesAdapter.Listener>() {
	interface Listener {
		fun onLanguageClicked(language: Language)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_available_language_list, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bindView(languages[position])
	}

	override fun getItemCount() = languages.size

	fun loadLanguages(langs: List<Language>) {
		languages.clear()
		languages.addAll(langs)
		notifyDataSetChanged()
	}

	inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		private val languageName: TextView = view.findViewById(R.id.languageLabel)
		private val flagImage: ImageView = view.findViewById(R.id.flagImageView)
		private val checkedImage: ImageView = view.findViewById(R.id.checkedImage)

		private lateinit var language: Language

		init {
			view.setOnClickListener { v: View? ->
				if (selectedLanguages.contains(language)) {
					selectedLanguages.remove(language)
				} else {
					selectedLanguages.add(language)
				}
				for (listener in listeners)
					listener.onLanguageClicked(language)
				notifyDataSetChanged()
			}
		}

		fun bindView(language: Language) {
			this.language = language
			if (selectedLanguages.contains(language)) {
				checkedImage.visibility = View.VISIBLE
				checkedImage.animate().scaleX(1f).setDuration(200).start()
			} else {
				checkedImage.animate().scaleX(0f).setDuration(200).start()
			}
			languageName.text = language.longName
			flagImage.setImageResource(language.languageType.drawable)
		}
	}
}