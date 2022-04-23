package ch.rmy.android.http_shortcuts.activities.main

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.context
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
            binding.description.isVisible = item.description.isNotEmpty()
            binding.icon.setIcon(item.icon, animated = isUpdate)
            if (isUpdate) {
                binding.waitingIcon.zoomToggle(item.isPending)
            } else {
                binding.waitingIcon.isVisible = item.isPending
            }
            val primaryColor = getPrimaryTextColor(context, item.textColor)
            binding.waitingIcon.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.name.setTextColor(primaryColor)
            binding.description.setTextColor(getSecondaryTextColor(context, item.textColor))
            if (item.useTextShadow) {
                val shadowColor = getTextShadowColor(context, item.textColor)
                binding.name.setShadowLayer(1f, 2f, 2f, shadowColor)
                binding.description.setShadowLayer(1f, 2f, 2f, shadowColor)
            } else {
                binding.name.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                binding.description.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
            }
        }
    }
}
