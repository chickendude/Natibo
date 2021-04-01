package ch.ralena.natibo.ui.course.create.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import ch.ralena.natibo.ui.callback.ItemTouchHelperCallback.ItemTouchHelperAdapter
import java.util.*
import javax.inject.Inject

class SelectedLanguagesAdapter @Inject constructor(
		private val languages: ArrayList<Language>,
) : BaseRecyclerAdapter<SelectedLanguagesAdapter.ViewHolder, SelectedLanguagesAdapter.Listener>(),
		ItemTouchHelperAdapter {
	interface Listener {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_language_list, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bindView(languages[position], position)
	}

	override fun getItemCount(): Int {
		return languages.size
	}

	override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
		val start: Int
		val count: Int
		if (fromPosition < toPosition) {
			start = fromPosition
			count = toPosition - fromPosition
			for (i in fromPosition until toPosition) {
				Collections.swap(languages, i, i + 1)
			}
		} else {
			start = toPosition
			count = fromPosition - toPosition
			for (i in fromPosition downTo toPosition + 1) {
				Collections.swap(languages, i, i - 1)
			}
		}
		notifyItemMoved(fromPosition, toPosition)
		notifyItemRangeChanged(start, count + 1)
		return true
	}

	// This isn't actually used here as swiping is disabled.
	override fun onItemDismiss(position: Int) {
		languages.removeAt(position)
		notifyItemRemoved(position)
	}

	fun addLanguage(language: Language) {
		languages.add(language)
		notifyItemInserted(languages.size)
	}

	fun removeLanguage(language: Language) {
		val index = languages.indexOf(language)
		languages.remove(language)
		notifyItemRemoved(index)
	}

	@SuppressLint("ClickableViewAccessibility")
	inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		private val languageName: TextView
		private val flagImage: ImageView
		private var language: Language? = null

		init {
			languageName = view.findViewById(R.id.languageLabel)
			flagImage = view.findViewById(R.id.flagImageView)
		}

		fun bindView(language: Language, position: Int) {
			this.language = language
			if (position == 0)
				languageName.text = String.format(Locale.getDefault(), view.resources.getString(R.string.base), language.longName)
			else
				languageName.text = String.format(Locale.getDefault(), view.resources.getString(R.string.target), language.longName, position)
			flagImage.setImageResource(language.languageType.drawable)
		}
	}
}