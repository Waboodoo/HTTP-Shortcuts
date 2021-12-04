package ch.rmy.android.http_shortcuts.activities

import androidx.recyclerview.widget.RecyclerView

abstract class SimpleListAdapter<T, U : RecyclerView.ViewHolder> : RecyclerView.Adapter<U>() {

    var items: List<T> = emptyList()

    init {
        setHasStableIds(true)
    }

    final override fun getItemCount() = items.size

    protected abstract fun getItemId(item: T): Long
}
