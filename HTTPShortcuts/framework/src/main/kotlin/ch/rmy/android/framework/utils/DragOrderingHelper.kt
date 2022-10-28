package ch.rmy.android.framework.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class DragOrderingHelper<ID : Any>(
    allowHorizontalDragging: Boolean = false,
    isEnabledCallback: () -> Boolean = { true },
    getId: (viewHolder: RecyclerView.ViewHolder) -> ID?,
) {

    private val movementChannel = Channel<Pair<ID, ID>>(capacity = Channel.UNLIMITED)

    val movementSource: Flow<Pair<ID, ID>> = movementChannel.receiveAsFlow()

    private val directions = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
        (if (allowHorizontalDragging) ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT else 0)

    private val callback = object : ItemTouchHelper.SimpleCallback(directions, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val id1 = getId(viewHolder) ?: return false
            val id2 = getId(target) ?: return false
            movementChannel.trySend(id1 to id2)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        override fun isItemViewSwipeEnabled() = false

        override fun isLongPressDragEnabled() = isEnabledCallback()
    }

    private val touchHelper = ItemTouchHelper(callback)

    fun attachTo(recyclerView: RecyclerView) {
        touchHelper.attachToRecyclerView(recyclerView)
    }
}
