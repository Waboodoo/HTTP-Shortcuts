package ch.rmy.android.http_shortcuts.activities.main

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.zoomToggle
import ch.rmy.android.http_shortcuts.databinding.GridItemShortcutBinding

class ShortcutGridAdapter : BaseShortcutAdapter() {

    override fun createViewHolder(parent: ViewGroup, layoutInflater: LayoutInflater) =
        ShortcutViewHolder(GridItemShortcutBinding.inflate(layoutInflater, parent, false))

    inner class ShortcutViewHolder(
        private val binding: GridItemShortcutBinding,
    ) : BaseShortcutViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                userEventChannel.trySend(UserEvent.ShortcutClicked(shortcutId))
            }
            binding.root.setOnLongClickListener {
                if (isLongClickingEnabled) {
                    consume {
                        userEventChannel.trySend(UserEvent.ShortcutLongClicked(shortcutId))
                    }
                } else {
                    false
                }
            }
        }

        override fun setItem(item: ShortcutListItem.Shortcut, isUpdate: Boolean) {
            binding.name.text = item.name
            binding.icon.setIcon(item.icon, animated = isUpdate)
            if (isUpdate) {
                binding.waitingIcon.zoomToggle(item.isPending)
            } else {
                binding.waitingIcon.isVisible = item.isPending
            }
            val primaryColor = getPrimaryTextColor(context, item.textColor)
            binding.waitingIcon.imageTintList = ColorStateList.valueOf(primaryColor)
            binding.name.setTextColor(primaryColor)
            if (item.useTextShadow) {
                binding.name.setShadowLayer(1f, 2f, 2f, getTextShadowColor(context, item.textColor))
            } else {
                binding.name.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
            }
        }
    }
}
