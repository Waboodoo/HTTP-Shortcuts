package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener
import ch.rmy.android.http_shortcuts.realm.models.HasId
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.realm.RealmChangeListener
import io.realm.RealmList
import io.realm.RealmObject

abstract class BaseAdapter<T> internal constructor(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Destroyable where T : RealmObject, T : HasId {

    var clickListener: OnItemClickedListener<T>? = null
    private var items: RealmList<T>? = null

    private val changeListener = RealmChangeListener<RealmList<T>> { notifyDataSetChanged() }

    init {
        setHasStableIds(true)
    }

    fun setItems(items: RealmList<T>) {
        this.items?.removeChangeListener(changeListener)
        this.items = items
        this.items?.addChangeListener(changeListener)
        notifyDataSetChanged()
    }

    override fun destroy() {
        if (this.items?.isValid == true) {
            this.items?.removeChangeListener(changeListener)
        }
    }

    override fun getItemViewType(position: Int) = if (isEmpty) TYPE_EMPTY_MARKER else TYPE_ITEM

    private fun getItem(position: Int) = items!![position]

    override fun getItemId(position: Int) = if (isEmpty) ID_EMPTY_MARKER else getItem(position).id

    override fun getItemCount() = if (isEmpty) 1 else count

    protected val count: Int
        get() = if (items == null || !items!!.isValid) 0 else items!!.size

    protected val isEmpty: Boolean
        get() = count == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_EMPTY_MARKER) EmptyMarkerViewHolder(parent, emptyMarkerStringResource) else createViewHolder(parent)
    }

    protected abstract fun createViewHolder(parentView: ViewGroup): BaseViewHolder<*>

    internal open val emptyMarkerStringResource = 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BaseViewHolder<*>) {
            (holder as BaseViewHolder<T>).setItem(getItem(position))
        }
    }

    private inner class EmptyMarkerViewHolder internal constructor(parent: ViewGroup, @StringRes textRes: Int) : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_empty_item, parent, false)) {

        init {
            if (textRes != 0) {
                (itemView.findViewById(R.id.empty_marker) as TextView).setText(textRes)
            }
        }
    }

    companion object {

        private const val TYPE_ITEM = 0
        private const val TYPE_EMPTY_MARKER = 1

        private const val ID_EMPTY_MARKER = -1L
    }

}
