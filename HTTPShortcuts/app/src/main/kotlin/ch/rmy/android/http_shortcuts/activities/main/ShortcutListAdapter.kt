package ch.rmy.android.http_shortcuts.activities.main

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.extensions.zoomToggle
import ch.rmy.android.http_shortcuts.databinding.ListItemShortcutBinding

class ShortcutListAdapter : BaseShortcutAdapter() {

    override fun createViewHolder(parent: ViewGroup, layoutInflater: LayoutInflater) =
        ShortcutViewHolder(ListItemShortcutBinding.inflate(layoutInflater, parent, false))

    inner class ShortcutViewHolder(
        private val binding: ListItemShortcutBinding,
    ) : BaseShortcutViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                userEventSubject.onNext(UserEvent.ShortcutClicked(shortcutId))
            }
            binding.root.setOnLongClickListener {
                if (isLongClickingEnabled) {
                    consume {
                        userEventSubject.onNext(UserEvent.ShortcutLongClicked(shortcutId))
                    }
                } else {
                    false
                }
            }
        }

        override fun setItem(item: ShortcutListItem.Shortcut, isUpdate: Boolean) {
            binding.name.text = item.name
            binding.description.text = item.description
            binding.description.visible = item.description.isNotEmpty()
            binding.icon.setIcon(item.icon, animated = isUpdate)
            if (isUpdate) {
                binding.waitingIcon.zoomToggle(item.isPending)
            } else {
                binding.waitingIcon.visible = item.isPending
            }
            val primaryColor = getPrimaryTextColor(context, item.textColor)
            binding.waitingIcon.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.name.setTextColor(primaryColor)
            binding.description.setTextColor(getSecondaryTextColor(context, item.textColor))
        }
    }
}
