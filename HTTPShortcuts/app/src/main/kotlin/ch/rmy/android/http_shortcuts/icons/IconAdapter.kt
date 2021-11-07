package ch.rmy.android.http_shortcuts.icons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.databinding.IconListItemBinding

class IconAdapter(
    private val icons: List<ShortcutIcon>,
    private val listener: (ShortcutIcon) -> Unit,
) : RecyclerView.Adapter<IconViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder =
        IconViewHolder(IconListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.setIcon(icons[position])
    }

    override fun getItemCount() = icons.size

}