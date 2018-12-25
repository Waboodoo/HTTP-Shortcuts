package ch.rmy.android.http_shortcuts.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.dimen
import kotterknife.bindView

class CategoryAdapter(context: Context) : BaseAdapter<Category>(context) {

    override fun createViewHolder(parentView: ViewGroup) = CategoryViewHolder(parentView)

    inner class CategoryViewHolder(parent: ViewGroup) : BaseViewHolder<Category>(parent, R.layout.list_item_category, this@CategoryAdapter) {

        private val name: TextView by bindView(R.id.name)
        private val description: TextView by bindView(R.id.description)
        private val smallIconContainer: ViewGroup by bindView(R.id.small_icons)
        private val layoutTypeIcon: ImageView by bindView(R.id.layout_type_icon)

        override fun updateViews(item: Category) {
            name.text = item.name
            val count = item.shortcuts.size
            description.text = context.resources.getQuantityString(R.plurals.shortcut_count, count, count)

            updateIcons(item.shortcuts)
            updateLayoutTypeIcon(item.layoutType)
        }

        private fun updateIcons(shortcuts: List<Shortcut>) {
            updateIconNumber(Math.min(shortcuts.size, MAX_ICONS))
            var i = 0
            for (shortcut in shortcuts) {
                val icon = smallIconContainer.getChildAt(i) as IconView
                icon.setImageURI(shortcut.getIconURI(context), shortcut.iconName)
                i++
                if (i >= MAX_ICONS) {
                    break
                }
            }
        }

        private fun updateIconNumber(number: Int) {
            val size = dimen(context, R.dimen.small_icon_size)
            while (smallIconContainer.childCount < number) {
                val icon = IconView(context)
                icon.layoutParams = LinearLayout.LayoutParams(size, size)
                smallIconContainer.addView(icon)
            }
            while (smallIconContainer.childCount > number) {
                smallIconContainer.removeViewAt(0)
            }
        }

        private fun updateLayoutTypeIcon(layoutType: String) {
            when (layoutType) {
                Category.LAYOUT_GRID -> layoutTypeIcon.setImageResource(R.drawable.ic_grid)
                else -> layoutTypeIcon.setImageResource(R.drawable.ic_list)
            }
        }

    }

    companion object {

        private const val MAX_ICONS = 5
    }

}
