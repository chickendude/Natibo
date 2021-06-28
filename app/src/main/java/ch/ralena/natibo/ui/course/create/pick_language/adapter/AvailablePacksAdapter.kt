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
	interface Listener {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view: View =
			LayoutInflater.from(parent.context).inflate(R.layout.item_pack, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bindView(packs[position], position)
	}

	override fun getItemCount(): Int {
		return packs.size
	}

	fun loadPacks(packs: List<PackRoom>) {
		this.packs.clear()
		this.packs.addAll(packs)
		notifyDataSetChanged()
	}

	@SuppressLint("ClickableViewAccessibility")
	inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
		private val packName: TextView = view.findViewById(R.id.pack_name_label)
		private var pack: PackRoom? = null

		init {
			// todo: notify listeners of click
			view.setOnClickListener { }
		}

		fun bindView(pack: PackRoom, position: Int) {
			this.pack = pack
			packName.text = pack.name
		}
	}
}