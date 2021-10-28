package ch.rmy.android.http_shortcuts.activities.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.GridItemShortcutBinding

class ShortcutGridAdapter(context: Context, shortcuts: ListLiveData<Shortcut>) : BaseShortcutAdapter(context, shortcuts) {

    override fun createViewHolder(parentView: ViewGroup) =
        ShortcutViewHolder(GridItemShortcutBinding.inflate(LayoutInflater.from(parentView.context), parentView, false))

    inner class ShortcutViewHolder(private val binding: GridItemShortcutBinding) :
        BaseViewHolder<Shortcut>(binding.root, this@ShortcutGridAdapter) {

        override fun updateViews(item: Shortcut) {
            binding.name.text = item.name
            binding.icon.setIcon(item.icon)
            binding.name.setTextColor(nameTextColor)
        }

    }

}
