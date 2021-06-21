package ch.ralena.natibo.ui.course.create.pick_schedule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.Pack
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter

class PackAdapter(
	private val targetPacks: ArrayList<Pack>,
	private val packs: ArrayList<Pack>
) : BaseRecyclerAdapter<PackAdapter.ViewHolder, PackAdapter.Listener>() {
	interface Listener {
		fun onBookClicked(pack: Pack)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_course_detail, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		if (position < itemCount) holder.bindView(packs[position])
	}

	override fun getItemCount() = packs.size

	fun loadPacks(packs: List<Pack>) {
		this.packs.clear()
		this.packs.addAll(packs)
		notifyDataSetChanged()
	}

	fun toggleTargetPack(pack: Pack) {
		if (targetPacks.contains(pack))
			targetPacks.remove(pack)
		else
			targetPacks.add(pack)
		notifyDataSetChanged()
	}

	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val book: CheckedTextView = view.findViewById(R.id.packTitleLabel)

		private lateinit var pack: Pack

		init {
			view.setOnClickListener { for (l in listeners) l.onBookClicked(pack) }
		}

		fun bindView(pack: Pack) {
			this.pack = pack
			book.text = pack.book
			book.isChecked = targetPacks.contains(pack)
			val color: Int =
				if (book.isChecked) R.color.colorPrimaryDark else R.color.colorPrimaryLight
			book.setTextColor(ContextCompat.getColor(book.getContext(), color))
		}
	}
}