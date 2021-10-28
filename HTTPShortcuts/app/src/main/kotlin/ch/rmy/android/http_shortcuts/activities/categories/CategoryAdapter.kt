package ch.rmy.android.http_shortcuts.activities.categories

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ListItemCategoryBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.dimen
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconView
import kotlin.math.min

class CategoryAdapter(context: Context, categories: ListLiveData<Category>) : BaseAdapter<Category>(context, categories) {

    override fun createViewHolder(parentView: ViewGroup) =
        CategoryViewHolder(ListItemCategoryBinding.inflate(LayoutInflater.from(parentView.context), parentView, false))

    inner class CategoryViewHolder(
        private val binding: ListItemCategoryBinding,
    ) : BaseViewHolder<Category>(
        binding.root,
        this@CategoryAdapter,
    ) {

        override fun updateViews(item: Category) {
            binding.name.text = getName(item)
            val count = item.shortcuts.size
            binding.description.text = context.resources.getQuantityString(R.plurals.shortcut_count, count, count)

            updateIcons(item.shortcuts)
            updateLayoutTypeIcon(item.layoutType.takeUnless { item.hidden })
        }

        private fun getName(category: Category): String = if (category.hidden) {
            context.getString(R.string.label_category_hidden, category.name)
        } else {
            category.name
        }

        private fun updateIcons(shortcuts: List<Shortcut>) {
            updateIconNumber(min(shortcuts.size, MAX_ICONS))
            shortcuts
                .take(MAX_ICONS)
                .forEachIndexed { index, shortcut ->
                    val icon = binding.smallIcons.getChildAt(index) as IconView
                    icon.setIcon(shortcut.icon)
                }
        }

        private fun updateIconNumber(number: Int) {
            val size = dimen(context, R.dimen.small_icon_size)
            while (binding.smallIcons.childCount < number) {
                val icon = IconView(context)
                icon.layoutParams = LinearLayout.LayoutParams(size, size)
                binding.smallIcons.addView(icon)
            }
            while (binding.smallIcons.childCount > number) {
                binding.smallIcons.removeViewAt(0)
            }
        }

        private fun updateLayoutTypeIcon(layoutType: String?) {
            if (layoutType == null) {
                binding.layoutTypeIcon.visible = false
            } else {
                binding.layoutTypeIcon.visible = true
                binding.layoutTypeIcon.setImageResource(when (layoutType) {
                    Category.LAYOUT_GRID -> R.drawable.ic_grid
                    else -> R.drawable.ic_list
                })
                binding.layoutTypeIcon.applyTheme()
            }
        }

    }

    companion object {

        private const val MAX_ICONS = 5
    }

}
