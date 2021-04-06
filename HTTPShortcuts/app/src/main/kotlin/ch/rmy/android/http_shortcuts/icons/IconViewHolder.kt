package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import kotterknife.bindView

class IconViewHolder(context: Context, parent: ViewGroup, listener: (ShortcutIcon.BuiltInIcon) -> Unit)
    : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.icon_list_item, parent, false)) {

    private val iconView: IconView by bindView(R.id.icon)

    private lateinit var icon: ShortcutIcon.BuiltInIcon

    init {
        itemView.setOnClickListener {
            listener.invoke(icon)
        }
    }

    fun setIcon(icon: ShortcutIcon.BuiltInIcon) {
        this.icon = icon
        iconView.setIcon(icon)
    }

}
