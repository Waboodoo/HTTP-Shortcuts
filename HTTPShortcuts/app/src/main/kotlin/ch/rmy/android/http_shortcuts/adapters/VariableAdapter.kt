package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.findIndex
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.Variable
import kotterknife.bindView

class VariableAdapter(context: Context, variables: ListLiveData<Variable>) : BaseAdapter<Variable>(context, variables) {

    override fun createViewHolder(parentView: ViewGroup) = VariableViewHolder(parentView)

    override val emptyMarkerStringResource = R.string.no_variables

    inner class VariableViewHolder(parent: ViewGroup) : BaseViewHolder<Variable>(parent, R.layout.list_item_variable, this@VariableAdapter) {

        private val name: TextView by bindView(R.id.name)
        private val type: TextView by bindView(R.id.type)

        override fun updateViews(item: Variable) {
            name.text = item.key
            type.setText(Variable.TYPE_RESOURCES[Variable.TYPE_OPTIONS.findIndex(item.type)])
        }

    }
}
