package ch.rmy.android.http_shortcuts.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import kotterknife.bindView

class SelectVariableOptionsAdapter : RecyclerView.Adapter<SelectVariableOptionsAdapter.SelectOptionViewHolder>() {

    var options: List<Option> = emptyList()
    var clickListener: ((Option) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectOptionViewHolder(parent)

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: SelectOptionViewHolder, position: Int) {
        holder.updateViews(options[position])
    }

    inner class SelectOptionViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.select_option, parent, false)) {

        private val label: TextView by bindView(R.id.select_option_label)

        fun updateViews(option: Option) {
            label.text = option.label
            itemView.setOnClickListener { clickListener?.invoke(option) }
        }

    }
}