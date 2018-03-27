package ch.rmy.android.http_shortcuts.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.Variables
import kotterknife.bindView

class ToggleVariableOptionsAdapter : RecyclerView.Adapter<ToggleVariableOptionsAdapter.SelectOptionViewHolder>() {

    lateinit var variables: List<Variable>
    var variableColor: Int = 0

    var options: List<Option> = emptyList()
    var clickListener: ((Option) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectOptionViewHolder(parent)

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: SelectOptionViewHolder, position: Int) {
        holder.updateViews(options[position])
    }

    inner class SelectOptionViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.toggle_option, parent, false)) {

        private val value: TextView by bindView(R.id.toggle_option_value)

        fun updateViews(option: Option) {
            value.text = Variables.rawPlaceholdersToVariableSpans(option.value, variables, variableColor)
            itemView.setOnClickListener { clickListener?.invoke(option) }
        }

    }
}