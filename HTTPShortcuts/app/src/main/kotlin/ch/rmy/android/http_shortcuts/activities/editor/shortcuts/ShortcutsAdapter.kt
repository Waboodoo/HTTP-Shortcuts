package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItem
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItemId
import ch.rmy.android.http_shortcuts.databinding.ListEmptyItemBinding
import ch.rmy.android.http_shortcuts.databinding.ListItemShortcutTriggerBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ShortcutsAdapter : BaseAdapter<ShortcutListItem>() {

    sealed interface UserEvent {
        data class ShortcutClicked(val id: ShortcutListItemId) : UserEvent
    }

    private val userEventSubject = PublishSubject.create<UserEvent>()

    val userEvents: Observable<UserEvent>
        get() = userEventSubject

    override fun areItemsTheSame(oldItem: ShortcutListItem, newItem: ShortcutListItem): Boolean =
        when (oldItem) {
            is ShortcutListItem.Shortcut -> (newItem as? ShortcutListItem.Shortcut)?.id == oldItem.id
            is ShortcutListItem.EmptyState -> newItem is ShortcutListItem.EmptyState
        }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is ShortcutListItem.Shortcut -> VIEW_TYPE_SHORTCUT
            is ShortcutListItem.EmptyState -> VIEW_TYPE_EMPTY_STATE
        }

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder? =
        when (viewType) {
            VIEW_TYPE_SHORTCUT -> ShortcutViewHolder(ListItemShortcutTriggerBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_EMPTY_STATE -> EmptyStateViewHolder(ListEmptyItemBinding.inflate(layoutInflater, parent, false))
            else -> null
        }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: ShortcutListItem, payloads: List<Any>) {
        when (holder) {
            is EmptyStateViewHolder -> holder.setItem(item as ShortcutListItem.EmptyState)
            is ShortcutViewHolder -> holder.setItem(item as ShortcutListItem.Shortcut)
        }
    }

    inner class ShortcutViewHolder(
        private val binding: ListItemShortcutTriggerBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var id: ShortcutListItemId
            private set

        init {
            binding.root.setOnClickListener {
                userEventSubject.onNext(UserEvent.ShortcutClicked(id))
            }
        }

        fun setItem(shortcut: ShortcutListItem.Shortcut) {
            id = shortcut.id
            binding.name.setText(shortcut.name)
            binding.icon.setIcon(shortcut.icon)
        }
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
