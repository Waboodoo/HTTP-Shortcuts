package ch.rmy.android.framework.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class DragOrderingHelper<ID : Any>(
    allowHorizontalDragging: Boolean = false,
    isEnabledCallback: () -> Boolean = { true },
    getId: (viewHolder: RecyclerView.ViewHolder) -> ID?,
) {

    val movementSource: Observable<Pair<ID, ID>>
        get() = movementSubject

    private val movementSubject = PublishSubject.create<Pair<ID, ID>>()

    private val directions = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
        (if (allowHorizontalDragging) ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT else 0)

    private val callback = object : ItemTouchHelper.SimpleCallback(directions, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val id1 = getId(viewHolder) ?: return false
            val id2 = getId(target) ?: return false
            movementSubject.onNext(id1 to id2)
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
