package ch.rmy.android.http_shortcuts.icons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.databinding.IconListItemBinding

class BuiltInIconAdapter(
    private val icons: List<ShortcutIcon.BuiltInIcon>,
    private val listener: (ShortcutIcon.BuiltInIcon) -> Unit,
) : RecyclerView.Adapter<BuiltInIconAdapter.IconViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder =
        IconViewHolder(IconListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.setIcon(icons[position])
    }

    override fun getItemCount() = icons.size

    inner class IconViewHolder(
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
}
