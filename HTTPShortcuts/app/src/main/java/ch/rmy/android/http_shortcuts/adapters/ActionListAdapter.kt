package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import kotterknife.bindView

class ActionListAdapter(private val context: Context) : SimpleListAdapter<BaseAction, ActionListAdapter.ActionViewHolder>() {

    var actions: List<BaseAction>
        get() = items
        set(value) {
            items = value
        }

    var clickListener: ((action: BaseAction) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ActionViewHolder(parent)

    override fun getItemId(item: BaseAction) = UUIDUtils.toLong(item.id)

    inner class ActionViewHolder(parent: ViewGroup) : SimpleViewHolder<BaseAction>(parent, R.layout.list_item_action) {

        private val title: TextView by bindView(R.id.action_title)
        private val description: TextView by bindView(R.id.action_description)

        override fun updateViews(item: BaseAction) {
            title.text = item.actionType.title
            description.text = item.getDescription(context)
            itemView.setOnClickListener {
                clickListener?.invoke(item)
            }
        }

    }

}