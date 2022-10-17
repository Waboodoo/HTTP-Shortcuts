package ch.rmy.android.http_shortcuts.activities.icons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.databinding.IconPickerListItemBinding
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class IconPickerAdapter : BaseAdapter<IconPickerListItem>() {

    sealed interface UserEvent {
        data class IconClicked(val icon: ShortcutIcon.CustomIcon) : UserEvent
        data class IconLongClicked(val icon: ShortcutIcon.CustomIcon) : UserEvent
    }

    private val userEventChannel = Channel<UserEvent>(capacity = Channel.UNLIMITED)

    val userEvents: Flow<UserEvent> = userEventChannel.receiveAsFlow()

    override fun areItemsTheSame(oldItem: IconPickerListItem, newItem: IconPickerListItem): Boolean =
        oldItem.icon == newItem.icon

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder =
        IconViewHolder(IconPickerListItemBinding.inflate(layoutInflater, parent, false))

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: IconPickerListItem, payloads: List<Any>) {
        (holder as IconViewHolder).setItem(item)
    }

    inner class IconViewHolder(
        private val binding: IconPickerListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var icon: ShortcutIcon.CustomIcon

        init {
            binding.root.setOnClickListener {
                userEventChannel.trySend(UserEvent.IconClicked(icon))
            }
            binding.root.setOnLongClickListener {
                consume {
                    userEventChannel.trySend(UserEvent.IconLongClicked(icon))
                }
            }
        }

        fun setItem(item: IconPickerListItem) {
            this.icon = item.icon
            binding.icon.setIcon(item.icon)
            binding.icon.alpha = if (item.isUnused) ALPHA_UNUSED else ALPHA_USED
        }
    }

    companion object {
        private const val ALPHA_USED = 1f
        private const val ALPHA_UNUSED = 0.6f
    }
}
