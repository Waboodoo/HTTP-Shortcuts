package ch.rmy.android.http_shortcuts.adapters

import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import kotterknife.bindView

class SelectVariableOptionsAdapter : SimpleListAdapter<Option, SelectVariableOptionsAdapter.SelectOptionViewHolder>() {

    var options: List<Option>
        get() = items
        set(value) {
            items = value
        }
    var clickListener: ((Option) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectOptionViewHolder(parent)

    override fun getItemId(item: Option) = UUIDUtils.toLong(item.id)

    inner class SelectOptionViewHolder(parent: ViewGroup) : SimpleViewHolder<Option>(parent, R.layout.select_option) {

        private val label: TextView by bindView(R.id.select_option_label)

        override fun updateViews(item: Option) {
            label.text = item.label
            itemView.setOnClickListener { clickListener?.invoke(item) }
        }

    }
}