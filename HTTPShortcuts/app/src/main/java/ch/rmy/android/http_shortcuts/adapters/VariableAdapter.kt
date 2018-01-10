package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.ArrayUtil
import kotterknife.bindView

class VariableAdapter(context: Context) : BaseAdapter<Variable>(context) {

    override fun createViewHolder(parentView: ViewGroup) = VariableViewHolder(parentView)

    override val emptyMarkerStringResource = R.string.no_variables

    inner class VariableViewHolder(parent: ViewGroup) : BaseViewHolder<Variable>(parent, R.layout.list_item_variable, this@VariableAdapter) {

        private val name: TextView by bindView(R.id.name)
        private val type: TextView by bindView(R.id.type)

        override fun updateViews(item: Variable) {
            name.text = item.key
            type.setText(Variable.TYPE_RESOURCES[ArrayUtil.findIndex(Variable.TYPE_OPTIONS, item.type)])
        }

    }
}
