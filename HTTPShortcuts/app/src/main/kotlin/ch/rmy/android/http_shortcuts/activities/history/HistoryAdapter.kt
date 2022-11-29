package ch.rmy.android.http_shortcuts.activities.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.ListEmptyItemBinding
import ch.rmy.android.http_shortcuts.databinding.ListItemHistoryBinding
import java.text.SimpleDateFormat

class HistoryAdapter : BaseAdapter<HistoryListItem>() {

    private val dateFormat = SimpleDateFormat.getTimeInstance()

    override fun areItemsTheSame(oldItem: HistoryListItem, newItem: HistoryListItem): Boolean =
        when (oldItem) {
            is HistoryListItem.HistoryEvent -> (newItem as? HistoryListItem.HistoryEvent)?.id == oldItem.id
            is HistoryListItem.EmptyState -> newItem is HistoryListItem.EmptyState
        }

    override fun getChangePayload(oldItem: HistoryListItem, newItem: HistoryListItem): Any? {
        if (oldItem is HistoryListItem.HistoryEvent && newItem is HistoryListItem.HistoryEvent) {
            return Unit
        }
        return null
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is HistoryListItem.HistoryEvent -> VIEW_TYPE_HISTORY_EVENT
            is HistoryListItem.EmptyState -> VIEW_TYPE_EMPTY_STATE
        }

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder? =
        when (viewType) {
            VIEW_TYPE_HISTORY_EVENT -> HistoryEventViewHolder(ListItemHistoryBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_EMPTY_STATE -> EmptyStateViewHolder(ListEmptyItemBinding.inflate(layoutInflater, parent, false))
            else -> null
        }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: HistoryListItem, payloads: List<Any>) {
        when (holder) {
            is HistoryEventViewHolder -> holder.setItem(item as HistoryListItem.HistoryEvent)
            is EmptyStateViewHolder -> holder.setItem(item as HistoryListItem.EmptyState)
        }
    }

    inner class HistoryEventViewHolder(
        private val binding: ListItemHistoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: HistoryListItem.HistoryEvent) {
            binding.title.setText(item.title)
            binding.title.setTextColor(item.displayType.getColor())
            binding.time.text = dateFormat.format(item.time)
            binding.details.isVisible = item.detail != null
            binding.details.setText(item.detail)
        }

        @ColorInt
        private fun HistoryListItem.HistoryEvent.DisplayType?.getColor(): Int =
            color(
                context,
                when (this) {
                    HistoryListItem.HistoryEvent.DisplayType.SUCCESS -> R.color.history_text_color_success
                    HistoryListItem.HistoryEvent.DisplayType.FAILURE -> R.color.history_text_color_failure
                    else -> R.color.text_color_primary_dark
                }
            )
    }

    inner class EmptyStateViewHolder(
        private val binding: ListEmptyItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: HistoryListItem.EmptyState) {
            binding.emptyMarker.setText(item.title)
            binding.emptyMarkerInstructions.setText(item.instructions)
        }
    }

    companion object {
        private const val VIEW_TYPE_HISTORY_EVENT = 1
        private const val VIEW_TYPE_EMPTY_STATE = 2
    }
}
