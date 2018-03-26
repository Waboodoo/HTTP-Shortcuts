package ch.rmy.android.http_shortcuts.utils

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class DragOrderingHelper(isEnabledCallback: () -> Boolean = { true }) {

    val positionChangeSource = EventSource<Pair<Int, Int>>()

    private val callback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val oldPosition = viewHolder.adapterPosition
            val newPosition = target.adapterPosition
            if (oldPosition == RecyclerView.NO_POSITION || newPosition == RecyclerView.NO_POSITION) {
                return false
            }
            positionChangeSource.notifyObservers(oldPosition to newPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) = Unit

        override fun isItemViewSwipeEnabled() = false

        override fun isLongPressDragEnabled() = isEnabledCallback()
    }

    private val touchHelper = ItemTouchHelper(callback)

    fun attachTo(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(recyclerView)
    }


}