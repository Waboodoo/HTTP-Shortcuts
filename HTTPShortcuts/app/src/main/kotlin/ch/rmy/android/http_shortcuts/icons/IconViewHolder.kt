package ch.rmy.android.http_shortcuts.icons

import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.databinding.IconListItemBinding

class IconViewHolder(
    private val binding: IconListItemBinding,
    listener: (ShortcutIcon) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var icon: ShortcutIcon

    init {
        itemView.setOnClickListener {
            listener.invoke(icon)
        }
    }

    fun setIcon(icon: ShortcutIcon) {
        this.icon = icon
        binding.icon.setIcon(icon)
    }

}
