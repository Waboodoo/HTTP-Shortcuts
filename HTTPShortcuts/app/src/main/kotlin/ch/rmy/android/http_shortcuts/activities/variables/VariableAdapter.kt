package ch.rmy.android.http_shortcuts.activities.variables

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.activities.variables.VariableEditorActivity.Companion.VARIABLE_TYPES
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Variable
import kotterknife.bindView

class VariableAdapter(context: Context, variables: ListLiveData<Variable>) : BaseAdapter<Variable>(context, variables) {

    override fun createViewHolder(parentView: ViewGroup) = VariableViewHolder(parentView)

    override val emptyMarker = EmptyMarker(
        context.getString(R.string.empty_state_variables),
        context.getString(R.string.empty_state_variables_instructions)
    )

    inner class VariableViewHolder(parent: ViewGroup) : BaseViewHolder<Variable>(parent, R.layout.list_item_variable, this@VariableAdapter) {

        private val name: TextView by bindView(R.id.name)
        private val type: TextView by bindView(R.id.type)

        override fun updateViews(item: Variable) {
            name.text = item.key
            type.text = VARIABLE_TYPES
                .find { it.first == item.type }
                ?.second
                ?.let { context.getString(it) }
                ?: ""
        }

    }
}
