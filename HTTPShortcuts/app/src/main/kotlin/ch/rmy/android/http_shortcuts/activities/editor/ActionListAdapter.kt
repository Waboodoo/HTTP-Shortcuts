package ch.rmy.android.http_shortcuts.activities.editor

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.activities.SimpleListAdapter
import ch.rmy.android.http_shortcuts.activities.SimpleViewHolder
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import kotterknife.bindView

class ActionListAdapter(private val context: Context) : SimpleListAdapter<BaseAction, ActionListAdapter.ActionViewHolder>() {

    var actions: List<BaseAction>
        get() = items
        set(value) {
            items = value
        }

    var clickListener: ((action: BaseAction) -> Unit)? = null

    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ActionViewHolder(parent)

    override fun getItemId(item: BaseAction) = UUIDUtils.toLong(item.id)

    inner class ActionViewHolder(parent: ViewGroup) : SimpleViewHolder<BaseAction>(parent, R.layout.list_item_action) {

        private val title: TextView by bindView(R.id.action_title)
        private val description: TextView by bindView(R.id.action_description)

        override fun updateViews(item: BaseAction) {
            title.text = item.actionType.title
            description.text = Variables.rawPlaceholdersToVariableSpans(
                item.getDescription(context),
                variablePlaceholderProvider,
                color(context, R.color.variable)
            )
            itemView.setOnClickListener {
                clickListener?.invoke(item)
            }
        }

    }

}