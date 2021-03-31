package ch.ralena.natibo.ui.course.create.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Language
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import ch.ralena.natibo.ui.callback.ItemTouchHelperCallback.ItemTouchHelperAdapter
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class SelectedLanguagesAdapter @Inject constructor(
		private val languages: ArrayList<Language?>,
) : BaseRecyclerAdapter<SelectedLanguagesAdapter.ViewHolder, SelectedLanguagesAdapter.Listener>(), ItemTouchHelperAdapter {
	interface Listener {
		fun onStartDrag(holder: RecyclerView.ViewHolder)
	}

	var languageSubject = PublishSubject.create<Language?>()
	fun asObservable(): PublishSubject<Language?> {
		return languageSubject
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view: View
		view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_language_list, parent, false)
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

	override fun onItemDismiss(position: Int) {
		languages.removeAt(position)
		notifyItemRemoved(position)
	}

	// TODO: Add "addLanguage" and "removeLanguage" functions that only add/remove a single language
	fun loadLanguages(langs: List<Language>) {
		languages.clear()
		languages.addAll(langs)
		notifyDataSetChanged()
	}

	inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		private val languageName: TextView
		private val flagImage: ImageView
		private val handleImage: ImageView
		private var language: Language? = null
		fun bindView(language: Language?, position: Int) {
			this.language = language
			if (position == 0) languageName.text = String.format(Locale.getDefault(), view.resources.getString(R.string.base), language!!.longName) else languageName.text = String.format(Locale.getDefault(), view.resources.getString(R.string.target), language!!.longName, position)
			flagImage.setImageResource(language.languageType.drawable)
		}

		init {
			languageName = view.findViewById(R.id.languageLabel)
			flagImage = view.findViewById(R.id.flagImageView)
			handleImage = view.findViewById(R.id.handleImage)
			handleImage.setOnTouchListener { v: View?, event: MotionEvent ->
				if (event.action == MotionEvent.ACTION_DOWN) {
					for (listener in listeners)
						listener.onStartDrag(this@ViewHolder)
				}
				false
			}
			view.setOnClickListener { v: View? -> languageSubject.onNext(language!!) }
		}
	}
}