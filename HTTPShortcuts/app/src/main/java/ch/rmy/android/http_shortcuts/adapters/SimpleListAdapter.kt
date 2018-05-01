package ch.rmy.android.http_shortcuts.adapters

import android.support.v7.widget.RecyclerView

abstract class SimpleListAdapter<T, U : SimpleViewHolder<T>> : RecyclerView.Adapter<U>() {

    var items: List<T> = emptyList()

    init {
        setHasStableIds(true)
    }

    final override fun getItemCount() = items.size

    protected abstract fun getItemId(item: T): Long

    final override fun onBindViewHolder(holder: U, position: Int) {
        holder.updateViews(items[position])
    }

}