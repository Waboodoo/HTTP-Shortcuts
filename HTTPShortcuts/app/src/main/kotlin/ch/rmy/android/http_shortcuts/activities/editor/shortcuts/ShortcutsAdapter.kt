package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.databinding.ListItemShortcutTriggerBinding
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.UUIDUtils

class ShortcutsAdapter(lifecycleOwner: LifecycleOwner, private val shortcuts: ListLiveData<ShortcutPlaceholder>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        shortcuts.observe(lifecycleOwner, {
            notifyDataSetChanged()
        })
        setHasStableIds(true)
    }

    var itemClickListener: ((ShortcutPlaceholder) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ShortcutViewHolder(ListItemShortcutTriggerBinding.inflate(LayoutInflater.from(parent.context), parent, false), this)

    override fun getItemCount() = shortcuts.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ShortcutViewHolder).setItem(getItem(position))
    }

    private fun getItem(position: Int): ShortcutPlaceholder =
        shortcuts[position]!!

    override fun getItemId(position: Int): Long =
        UUIDUtils.toLong(getItem(position).id)

    class ShortcutViewHolder(
        private val binding: ListItemShortcutTriggerBinding,
        adapter: ShortcutsAdapter,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var item: ShortcutPlaceholder

        init {
            itemView.setOnClickListener {
                adapter.itemClickListener?.invoke(item)
            }
        }

        fun setItem(shortcut: ShortcutPlaceholder) {
            this.item = shortcut
            if (shortcut.isDeleted()) {
                val deleted = itemView.context.getString(R.string.placeholder_deleted_shortcut)
                binding.name.text = HTMLUtil.format("<i>$deleted</i>")
                binding.icon.setIcon(ShortcutIcon.NoIcon)
            } else {
                binding.name.text = shortcut.name
                binding.icon.setIcon(shortcut.icon)
            }
        }

    }

}