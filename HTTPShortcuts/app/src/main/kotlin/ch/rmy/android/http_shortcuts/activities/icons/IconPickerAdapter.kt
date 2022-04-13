package ch.rmy.android.http_shortcuts.activities.icons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.databinding.IconPickerListItemBinding
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class IconPickerAdapter : BaseAdapter<IconPickerListItem>() {

    sealed interface UserEvent {
        data class IconClicked(val icon: ShortcutIcon.CustomIcon) : UserEvent
        data class IconLongClicked(val icon: ShortcutIcon.CustomIcon) : UserEvent
    }

    private val userEventSubject = PublishSubject.create<UserEvent>()

    val userEvents: Observable<UserEvent>
        get() = userEventSubject

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
                userEventSubject.onNext(UserEvent.IconClicked(icon))
            }
            binding.root.setOnLongClickListener {
                consume {
                    userEventSubject.onNext(UserEvent.IconLongClicked(icon))
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
