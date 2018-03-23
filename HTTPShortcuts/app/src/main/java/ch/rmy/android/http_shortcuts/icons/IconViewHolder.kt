package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.R
import kotterknife.bindView

class IconViewHolder(context: Context, parent: ViewGroup, listener: (String) -> Unit) : RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.icon_list_item, parent, false)) {

    private val iconView: IconView by bindView(R.id.icon)

    init {
        itemView.setOnClickListener {
            listener(iconView.iconName!!)
        }
    }

    fun setIcon(iconResource: Int) {
        iconView.setImageResource(iconResource)
    }

}
