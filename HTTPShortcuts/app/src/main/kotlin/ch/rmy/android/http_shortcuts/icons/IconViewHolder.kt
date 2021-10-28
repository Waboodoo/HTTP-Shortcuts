package ch.rmy.android.http_shortcuts.icons

import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.databinding.IconListItemBinding

class IconViewHolder(
    private val binding: IconListItemBinding,
    listener: (ShortcutIcon.BuiltInIcon) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var icon: ShortcutIcon.BuiltInIcon

    init {
        itemView.setOnClickListener {
            listener.invoke(icon)
        }
    }

    fun setIcon(icon: ShortcutIcon.BuiltInIcon) {
        this.icon = icon
        binding.icon.setIcon(icon)
    }

}
