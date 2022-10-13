package ch.ralena.natibo.ui.course.create.pick_language.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.LanguageRoom
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter

class NativeLanguagesAdapter(
	private val languages: ArrayList<LanguageRoom>,
) : BaseRecyclerAdapter<NativeLanguagesAdapter.ViewHolder, NativeLanguagesAdapter.Listener>() {
	interface Listener {
		fun onNativeLanguageClicked(language: LanguageRoom)
	}

	private var selectedLanguage: LanguageRoom? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_available_language_list, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bindView(languages[position])
	}

	override fun getItemCount() = languages.size

	fun loadLanguagesWithPacks(langs: List<LanguageRoom>) {
		languages.clear()
		languages.addAll(langs)
		notifyDataSetChanged()
	}

	fun setSelectedLanguage(language: LanguageRoom?) {
		selectedLanguage = language
		notifyDataSetChanged()
	}

	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val languageName: TextView = view.findViewById(R.id.languageLabel)
		private val flagImage: ImageView = view.findViewById(R.id.flag_image)
		private val checkedImage: ImageView = view.findViewById(R.id.checkedImage)

		private lateinit var language: LanguageRoom

		init {
			view.setOnClickListener {
				listeners.forEach { it.onNativeLanguageClicked(language) }
			}
		}

		fun bindView(language: LanguageRoom) {
			this.language = language
			if (language == selectedLanguage) {
				checkedImage.visibility = View.VISIBLE
				checkedImage.animate().scaleX(1f).setDuration(200).start()
			} else {
				checkedImage.animate().scaleX(0f).setDuration(200).start()
			}
			languageName.text = language.name
			flagImage.setImageResource(language.flagDrawable)
		}
	}
}