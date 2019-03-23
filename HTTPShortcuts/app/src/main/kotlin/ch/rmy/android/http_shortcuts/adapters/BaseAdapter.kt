package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.HasId
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.realm.RealmObject

abstract class BaseAdapter<T> internal constructor(val context: Context, private val items: ListLiveData<T>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Destroyable where T : RealmObject, T : HasId {

    var clickListener: ((LiveData<T?>) -> Unit)? = null
    var longClickListener: ((LiveData<T?>) -> Boolean)? = null

    private val observer = Observer<List<T>> { notifyDataSetChanged() }

    init {
        setHasStableIds(true)
        items.observeForever(observer)
    }

    override fun destroy() {
        items.removeObserver(observer)
    }

    override fun getItemViewType(position: Int) = if (isEmpty) TYPE_EMPTY_MARKER else TYPE_ITEM

    private fun getItem(position: Int) = items[position]!!

    override fun getItemId(position: Int) = if (isEmpty) ID_EMPTY_MARKER else getItem(position).id

    override fun getItemCount() = if (isEmpty && emptyMarkerStringResource != 0) 1 else count

    private val count: Int
        get() = items.size

    protected val isEmpty: Boolean
        get() = items.isEmpty()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_EMPTY_MARKER) EmptyMarkerViewHolder(parent, emptyMarkerStringResource) else createViewHolder(parent)

    protected abstract fun createViewHolder(parentView: ViewGroup): BaseViewHolder<*>

    internal open val emptyMarkerStringResource = 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BaseViewHolder<*>) {
            @Suppress("UNCHECKED_CAST")
            (holder as BaseViewHolder<T>).setItem(getItem(position))
        }
    }

    private inner class EmptyMarkerViewHolder internal constructor(parent: ViewGroup, @StringRes textRes: Int) : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_empty_item, parent, false)) {

        init {
            if (textRes != 0) {
                itemView.findViewById<TextView>(R.id.empty_marker).setText(textRes)
            }
        }
    }

    companion object {

        private const val TYPE_ITEM = 0
        private const val TYPE_EMPTY_MARKER = 1

        private const val ID_EMPTY_MARKER = -1L
    }

}
