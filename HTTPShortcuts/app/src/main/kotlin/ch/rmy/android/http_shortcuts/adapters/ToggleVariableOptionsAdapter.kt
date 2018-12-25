package ch.rmy.android.http_shortcuts.adapters

import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import kotterknife.bindView

class ToggleVariableOptionsAdapter : SimpleListAdapter<Option, ToggleVariableOptionsAdapter.SelectOptionViewHolder>() {

    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    var options: List<Option>
        get() = items
        set(value) {
            items = value
        }
    var clickListener: ((Option) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectOptionViewHolder(parent)

    override fun getItemId(item: Option) = UUIDUtils.toLong(item.id)

    inner class SelectOptionViewHolder(parent: ViewGroup) : SimpleViewHolder<Option>(parent, R.layout.toggle_option) {

        private val value: TextView by bindView(R.id.toggle_option_value)

        override fun updateViews(item: Option) {
            value.text = Variables.rawPlaceholdersToVariableSpans(item.value, variablePlaceholderProvider)
            itemView.setOnClickListener { clickListener?.invoke(item) }
        }

    }
}