package ch.ralena.natibo.ui.base

import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter<VH: RecyclerView.ViewHolder, LISTENER>: RecyclerView.Adapter<VH>() {
	val listeners = HashSet<LISTENER>()

	fun registerListener(listener: LISTENER) {
		listeners.add(listener)
	}

	fun unregisterListener(listener: LISTENER) {
		listeners.remove(listener)
	}
}