package ch.ralena.natibo.ui.course.create.pick_language.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.ralena.natibo.R
import ch.ralena.natibo.data.room.`object`.PackRoom
import ch.ralena.natibo.ui.base.BaseRecyclerAdapter
import ch.ralena.natibo.ui.callback.ItemTouchHelperCallback.ItemTouchHelperAdapter
import java.util.*
import javax.inject.Inject

class AvailablePacksAdapter @Inject constructor(
	private val packs: ArrayList<PackRoom>,
) : BaseRecyclerAdapter<
		AvailablePacksAdapter.ViewHolder,
		AvailablePacksAdapter.Listener>() {
	interface Listener {
		fun onPackSelected(pack: PackRoom)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view: View =
			LayoutInflater.from(parent.context).inflate(R.layout.item_pack, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bindView(packs[position])
	}

	override fun getItemCount(): Int {
		return packs.size
	}

	fun loadPacks(packs: List<PackRoom>) {
		this.packs.clear()
		this.packs.addAll(packs)
		notifyDataSetChanged()
	}

	inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val packName: TextView = view.findViewById(R.id.pack_name_label)

		private lateinit var pack: PackRoom

		init {
			view.setOnClickListener {
				listeners.forEach { it.onPackSelected(pack) }
			}
		}

		fun bindView(pack: PackRoom) {
			this.pack = pack
			packName.text = pack.name
		}
	}
}