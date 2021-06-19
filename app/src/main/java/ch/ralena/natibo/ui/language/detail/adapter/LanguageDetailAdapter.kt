package ch.ralena.natibo.ui.language.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import io.reactivex.subjects.PublishSubject
import io.realm.RealmList
import javax.inject.Inject

class LanguageDetailAdapter @Inject constructor(
	private val packs: ArrayList<PackRoom>
	) :
		BaseRecyclerAdapter<LanguageDetailAdapter.ViewHolder, LanguageDetailAdapter.Listener>() {
	interface Listener {
		fun onLanguagePackClicked(pack: PackRoom)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language_detail, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (position < itemCount) holder.bindView(packs[position])
	}

	override fun getItemCount(): Int {
		return packs.size
	}

	fun loadLanguagePacks(packs: List<PackRoom>) {
		this.packs.clear()
		this.packs.addAll(packs)
		notifyDataSetChanged()
	}

	inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		private val book: TextView
		private val numSentences: TextView
		private lateinit var pack: PackRoom

		fun bindView(pack: PackRoom) {
			this.pack = pack
			book.text = pack.name
			// todo show sentence count
//			numSentences.text = pack.sentences.size.toString()
		}

		init {
			book = view.findViewById(R.id.packTitleLabel)
			numSentences = view.findViewById(R.id.numSentencesLabel)
			view.setOnClickListener {
				for (l in listeners)
					l.onLanguagePackClicked(pack)
			}
		}
	}
}