package ch.rmy.android.http_shortcuts.activities.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.databinding.ListEmptyItemBinding
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseShortcutAdapter : BaseAdapter<ShortcutListItem>() {

    sealed interface UserEvent {
        data class ShortcutClicked(val id: String) : UserEvent
        data class ShortcutLongClicked(val id: String) : UserEvent
    }

    protected val userEventChannel = Channel<UserEvent>(capacity = Channel.UNLIMITED)

    val userEvents: Flow<UserEvent> = userEventChannel.receiveAsFlow()

    var isLongClickingEnabled = false

    override fun areItemsTheSame(oldItem: ShortcutListItem, newItem: ShortcutListItem): Boolean =
        when (oldItem) {
            is ShortcutListItem.Shortcut -> (newItem as? ShortcutListItem.Shortcut)?.id == oldItem.id
            is ShortcutListItem.EmptyState -> newItem is ShortcutListItem.EmptyState
        }

    override fun getChangePayload(oldItem: ShortcutListItem, newItem: ShortcutListItem): Any? {
        if (oldItem is ShortcutListItem.Shortcut && newItem is ShortcutListItem.Shortcut) {
            return Unit
        }
        return null
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is ShortcutListItem.Shortcut -> VIEW_TYPE_SHORTCUT
            is ShortcutListItem.EmptyState -> VIEW_TYPE_EMPTY_STATE
        }

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder? =
        when (viewType) {
            VIEW_TYPE_SHORTCUT -> createViewHolder(parent, layoutInflater)
            VIEW_TYPE_EMPTY_STATE -> EmptyStateViewHolder(ListEmptyItemBinding.inflate(layoutInflater, parent, false))
            else -> null
        }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: ShortcutListItem, payloads: List<Any>) {
        when (holder) {
            is EmptyStateViewHolder -> holder.setItem(item as ShortcutListItem.EmptyState)
            else -> (holder as BaseShortcutViewHolder).setItem(item as ShortcutListItem.Shortcut)
        }
    }

    protected abstract fun createViewHolder(parent: ViewGroup, layoutInflater: LayoutInflater): BaseShortcutViewHolder

    protected fun getPrimaryTextColor(context: Context, textColor: ShortcutListItem.TextColor) =
        when (textColor) {
            ShortcutListItem.TextColor.BRIGHT -> color(context, R.color.text_color_primary_bright)
            ShortcutListItem.TextColor.DARK -> color(context, R.color.text_color_primary_dark)
        }

    protected fun getSecondaryTextColor(context: Context, textColor: ShortcutListItem.TextColor) =
        when (textColor) {
            ShortcutListItem.TextColor.BRIGHT -> color(context, R.color.text_color_secondary_bright)
            ShortcutListItem.TextColor.DARK -> color(context, R.color.text_color_secondary_dark)
        }

    protected fun getTextShadowColor(context: Context, textColor: ShortcutListItem.TextColor) =
        when (textColor) {
            ShortcutListItem.TextColor.BRIGHT -> color(context, R.color.text_color_shadow_bright)
            ShortcutListItem.TextColor.DARK -> color(context, R.color.text_color_shadow_dark)
        }

    abstract class BaseShortcutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var shortcutId: ShortcutId
            protected set

        private var previousShortcutId: ShortcutId? = null

        fun setItem(item: ShortcutListItem.Shortcut) {
            shortcutId = item.id
            setItem(item, isUpdate = previousShortcutId == shortcutId)
            previousShortcutId = shortcutId
        }

        abstract fun setItem(item: ShortcutListItem.Shortcut, isUpdate: Boolean)
    }

    inner class EmptyStateViewHolder(
        private val binding: ListEmptyItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: ShortcutListItem.EmptyState) {
            binding.emptyMarker.setText(item.title)
            binding.emptyMarkerInstructions.setText(item.instructions)
        }
    }

    companion object {
        private const val VIEW_TYPE_SHORTCUT = 1
        private const val VIEW_TYPE_EMPTY_STATE = 2
    }
}
