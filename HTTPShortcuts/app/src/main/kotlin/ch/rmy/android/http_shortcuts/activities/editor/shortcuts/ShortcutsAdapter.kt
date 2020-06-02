package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import kotterknife.bindView

class ShortcutsAdapter(private val lifecycleOwner: LifecycleOwner, private val shortcuts: ListLiveData<ShortcutPlaceholder>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        shortcuts.observe(lifecycleOwner, Observer {
            notifyDataSetChanged()
        })
        setHasStableIds(true)
    }

    var itemClickListener: ((ShortcutPlaceholder) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ShortcutViewHolder(parent, R.layout.list_item_shortcut_trigger, this)

    override fun getItemCount() = shortcuts.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ShortcutViewHolder).setItem(getItem(position))
    }

    private fun getItem(position: Int): ShortcutPlaceholder =
        shortcuts[position]!!

    override fun getItemId(position: Int): Long =
        UUIDUtils.toLong(getItem(position).id)

    class ShortcutViewHolder(parent: ViewGroup, layoutRes: Int, adapter: ShortcutsAdapter) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {

        private val icon: IconView by bindView(R.id.icon)
        private val name: TextView by bindView(R.id.name)

        private lateinit var item: ShortcutPlaceholder

        init {
            itemView.setOnClickListener {
                adapter.itemClickListener?.invoke(item)
            }
        }

        fun setItem(shortcut: ShortcutPlaceholder) {
            this.item = shortcut
            name.setText(shortcut.name.ifBlank { "-" })
            icon.setIcon(shortcut.iconName)
        }

    }

}