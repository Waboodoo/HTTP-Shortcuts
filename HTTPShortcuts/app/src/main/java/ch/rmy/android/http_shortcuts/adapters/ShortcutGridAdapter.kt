package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.realm.models.Shortcut

class ShortcutGridAdapter(context: Context) : ShortcutAdapter(context) {

    override fun createViewHolder(parentView: ViewGroup) = ShortcutViewHolder(parentView)

    inner class ShortcutViewHolder(parent: ViewGroup) : BaseViewHolder<Shortcut>(parent, R.layout.shortcut_grid_item, this@ShortcutGridAdapter) {

        internal var name = itemView.findViewById(R.id.name) as TextView
        internal var icon = itemView.findViewById(R.id.icon) as IconView

        override fun updateViews(item: Shortcut) {
            name.text = item.name
            icon.setImageURI(item.getIconURI(context), item.iconName!!)
        }

    }

}
