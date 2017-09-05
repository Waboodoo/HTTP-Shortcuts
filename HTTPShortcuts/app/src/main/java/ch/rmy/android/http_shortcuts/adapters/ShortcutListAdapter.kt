package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.realm.models.Shortcut

class ShortcutListAdapter(context: Context) : ShortcutAdapter(context) {

    override fun createViewHolder(parentView: ViewGroup) = ShortcutViewHolder(parentView)

    inner class ShortcutViewHolder(parent: ViewGroup) : BaseViewHolder<Shortcut>(parent, R.layout.shortcut_list_item, this@ShortcutListAdapter) {

        internal var name = itemView.findViewById(R.id.name) as TextView
        internal var description = itemView.findViewById(R.id.description) as TextView
        internal var icon = itemView.findViewById(R.id.icon) as IconView
        internal var waitingIcon = itemView.findViewById(R.id.waiting_icon)

        override fun updateViews(item: Shortcut) {
            name.text = item.name
            description.text = item.description
            description.visibility = if (TextUtils.isEmpty(item.description)) View.GONE else View.VISIBLE
            icon.setImageURI(item.getIconURI(context), item.iconName)
            waitingIcon.visibility = if (isPendingExecution(item.id)) View.VISIBLE else View.GONE
        }

        private fun isPendingExecution(shortcutId: Long) = shortcutsPendingExecution!!.any {
            it.shortcutId == shortcutId
        }

    }

}
