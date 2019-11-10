package ch.rmy.android.http_shortcuts.activities.categories

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.dimen
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.utils.IconUtil
import kotterknife.bindView
import kotlin.math.min

class CategoryAdapter(context: Context, categories: ListLiveData<Category>) : BaseAdapter<Category>(context, categories) {

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
            updateIconNumber(min(shortcuts.size, MAX_ICONS))
            shortcuts
                .take(MAX_ICONS)
                .forEachIndexed { index, shortcut ->
                    val icon = smallIconContainer.getChildAt(index) as IconView
                    icon.setImageURI(IconUtil.getIconURI(context, shortcut.iconName), shortcut.iconName)
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
            layoutTypeIcon.setImageResource(when (layoutType) {
                Category.LAYOUT_GRID -> R.drawable.ic_grid
                else -> R.drawable.ic_list
            })
            layoutTypeIcon.applyTheme()
        }

    }

    companion object {

        private const val MAX_ICONS = 5
    }

}
