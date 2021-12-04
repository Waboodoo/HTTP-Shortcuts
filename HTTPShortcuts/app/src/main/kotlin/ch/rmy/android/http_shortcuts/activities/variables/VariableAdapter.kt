package ch.rmy.android.http_shortcuts.activities.variables

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.ListItemVariableBinding

class VariableAdapter(context: Context, variables: ListLiveData<Variable>) : BaseAdapter<Variable>(context, variables) {

    override fun createViewHolder(parentView: ViewGroup) =
        VariableViewHolder(ListItemVariableBinding.inflate(LayoutInflater.from(parentView.context), parentView, false))

    override val emptyMarker = EmptyMarker(
        context.getString(R.string.empty_state_variables),
        context.getString(R.string.empty_state_variables_instructions),
    )

    inner class VariableViewHolder(
        private val binding: ListItemVariableBinding,
    ) : BaseViewHolder<Variable>(binding.root, this@VariableAdapter) {

        override fun updateViews(item: Variable) {
            binding.name.text = item.key
            binding.type.setText(VariableTypes.getTypeName(item.type))
        }
    }
}
