package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import kotterknife.bindView

class ShortcutListAdapter(context: Context, shortcuts: ListLiveData<Shortcut>) : ShortcutAdapter(context, shortcuts) {

    override fun createViewHolder(parentView: ViewGroup) = ShortcutViewHolder(parentView)

    inner class ShortcutViewHolder(parent: ViewGroup) : BaseViewHolder<Shortcut>(parent, R.layout.list_item_shortcut, this@ShortcutListAdapter) {

        private val name: TextView by bindView(R.id.name)
        private val description: TextView by bindView(R.id.description)
        private val icon: IconView by bindView(R.id.icon)
        private val waitingIcon: View by bindView(R.id.waiting_icon)

        override fun updateViews(item: Shortcut) {
            name.text = item.name
            description.text = item.description
            description.visible = !TextUtils.isEmpty(item.description)
            icon.setImageURI(item.getIconURI(context), item.iconName)
            waitingIcon.visible = isPendingExecution(item.id)
        }

        private fun isPendingExecution(shortcutId: String) = shortcutsPendingExecution.any {
            it.shortcutId == shortcutId
        }

    }

}
