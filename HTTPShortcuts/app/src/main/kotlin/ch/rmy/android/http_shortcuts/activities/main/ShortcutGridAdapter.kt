package ch.rmy.android.http_shortcuts.activities.main

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.http_shortcuts.databinding.GridItemShortcutBinding

class ShortcutGridAdapter : BaseShortcutAdapter() {

    override fun createViewHolder(parent: ViewGroup, layoutInflater: LayoutInflater) =
        ShortcutViewHolder(GridItemShortcutBinding.inflate(layoutInflater, parent, false))

    inner class ShortcutViewHolder(
        private val binding: GridItemShortcutBinding,
    ) : BaseShortcutViewHolder(binding.root) {

        override lateinit var shortcutId: String
            private set

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

        override fun setItem(item: ShortcutListItem.Shortcut) {
            shortcutId = item.id
            binding.name.text = item.name
            binding.icon.setIcon(item.icon)
            binding.waitingIcon.visible = item.isPending
            val primaryColor = getPrimaryTextColor(context, item.textColor)
            binding.waitingIcon.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.name.setTextColor(primaryColor)
        }
    }
}
