package ch.ralena.natibo.callback;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
	public interface ItemTouchHelperAdapter {
		boolean onItemMove(int fromPosition, int toPosition);
		void onItemDismiss(int position);
	}

	ItemTouchHelperAdapter adapter;
	private boolean swipeToDismiss;

	public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter, boolean swipeToDismiss) {
		this.adapter = adapter;
		this.swipeToDismiss = swipeToDismiss;
	}

	@Override
	public boolean isLongPressDragEnabled() {
		return true;
	}

	@Override
	public boolean isItemViewSwipeEnabled() {
		return swipeToDismiss;
	}

	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
			final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
			final int swipeFlags = 0;
			return makeMovementFlags(dragFlags, swipeFlags);
		} else {
			final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
			final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
			return makeMovementFlags(dragFlags, swipeFlags);
		}
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
		if (source.getItemViewType() != target.getItemViewType()) {
			return false;
		}

		// Notify the adapter of the move
		adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
		return true;
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		adapter.onItemDismiss(viewHolder.getAdapterPosition());
	}
}
