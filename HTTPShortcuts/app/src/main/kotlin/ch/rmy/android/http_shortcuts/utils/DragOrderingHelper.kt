package ch.rmy.android.http_shortcuts.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class DragOrderingHelper(
    allowHorizontalDragging: Boolean = false,
    isEnabledCallback: () -> Boolean = { true },
) {

    val positionChangeSource: Observable<Pair<Int, Int>>
        get() = positionChangeSubject

    private val positionChangeSubject = PublishSubject.create<Pair<Int, Int>>()

    private val directions = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
        (if (allowHorizontalDragging) ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT else 0)

    private val callback = object : ItemTouchHelper.SimpleCallback(directions, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val oldPosition = viewHolder.adapterPosition
            val newPosition = target.adapterPosition
            if (oldPosition == RecyclerView.NO_POSITION || newPosition == RecyclerView.NO_POSITION) {
                return false
            }
            positionChangeSubject.onNext(oldPosition to newPosition)
            return true
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