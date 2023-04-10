package ch.rmy.android.framework.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

@Deprecated("Remove once fully migrated to Compose")
abstract class BaseAdapter<T : Any> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<T>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount(): Int =
        items.size

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    protected open fun areItemContentsTheSame(oldItem: T, newItem: T): Boolean =
        oldItem == newItem

    protected open fun getChangePayload(oldItem: T, newItem: T): Any? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        createViewHolder(viewType, parent, LayoutInflater.from(parent.context))
            ?: error("ViewHolder creation failed, not implemented?")

    protected abstract fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder?

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = items[position]
        bindViewHolder(holder, position, item, payloads)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        bindViewHolder(holder, position, item, emptyList())
    }

    protected abstract fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: T, payloads: List<Any>)

    private val differ = AsyncListDiffer(
        object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                notifyItemRangeChanged(position, count, payload)
            }
        },
        AsyncDifferConfig.Builder(
            object : DiffUtil.ItemCallback<T>() {
                override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
                    this@BaseAdapter.areItemsTheSame(oldItem, newItem)

                override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
                    this@BaseAdapter.areItemContentsTheSame(oldItem, newItem)

                override fun getChangePayload(oldItem: T, newItem: T): Any? =
                    this@BaseAdapter.getChangePayload(oldItem, newItem)
            },
        )
            .build(),
    )
}
