package ch.rmy.android.http_shortcuts.activities.variables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.activities.SimpleListAdapter
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.databinding.SelectOptionBinding
import ch.rmy.android.http_shortcuts.utils.UUIDUtils

class SelectVariableOptionsAdapter : SimpleListAdapter<Option, SelectVariableOptionsAdapter.SelectOptionViewHolder>() {

    var options: List<Option>
        get() = items
        set(value) {
            items = value
        }
    var clickListener: ((Option) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SelectOptionViewHolder(SelectOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemId(item: Option) = UUIDUtils.toLong(item.id)

    override fun onBindViewHolder(holder: SelectOptionViewHolder, position: Int) {
        holder.updateViews(options[position])
    }

    inner class SelectOptionViewHolder(
        private val binding: SelectOptionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun updateViews(item: Option) {
            binding.selectOptionLabel.text = item.labelOrValue
            itemView.setOnClickListener { clickListener?.invoke(item) }
        }

    }
}