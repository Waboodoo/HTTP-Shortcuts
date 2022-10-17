package ch.rmy.android.http_shortcuts.activities.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.dimen
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.databinding.ListItemCategoryBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class CategoryAdapter : BaseAdapter<CategoryListItem>() {

    sealed interface UserEvent {
        data class CategoryClicked(val id: String) : UserEvent
    }

    private val userEventChannel = Channel<UserEvent>(capacity = Channel.UNLIMITED)

    val userEvents: Flow<UserEvent> = userEventChannel.receiveAsFlow()

    override fun areItemsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem): Boolean =
        oldItem.id == newItem.id

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder =
        CategoryViewHolder(ListItemCategoryBinding.inflate(layoutInflater, parent, false))

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: CategoryListItem, payloads: List<Any>) {
        (holder as CategoryViewHolder).setItem(item)
    }

    inner class CategoryViewHolder(
        private val binding: ListItemCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var categoryId: CategoryId
            private set

        init {
            binding.root.setOnClickListener {
                userEventChannel.trySend(UserEvent.CategoryClicked(categoryId))
            }
        }

        fun setItem(item: CategoryListItem) {
            categoryId = item.id
            binding.name.setText(item.name)
            binding.description.setText(item.description)
            binding.name.alpha = if (item.hidden) 0.7f else 1f

            updateIcons(item.icons)
            updateLayoutTypeIcon(item.layoutType)
        }

        private fun updateIcons(icons: List<ShortcutIcon>) {
            updateIconNumber(icons.size)
            icons
                .forEachIndexed { index, shortcutIcon ->
                    val iconView = binding.smallIcons.getChildAt(index) as IconView
                    iconView.setIcon(shortcutIcon)
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

        private fun updateLayoutTypeIcon(layoutType: CategoryLayoutType?) {
            if (layoutType == null) {
                binding.layoutTypeIcon.isVisible = false
            } else {
                binding.layoutTypeIcon.isVisible = true
                binding.layoutTypeIcon.setImageResource(
                    when (layoutType) {
                        CategoryLayoutType.LINEAR_LIST -> R.drawable.ic_list
                        CategoryLayoutType.DENSE_GRID,
                        CategoryLayoutType.MEDIUM_GRID,
                        CategoryLayoutType.WIDE_GRID,
                        -> R.drawable.ic_grid
                    }
                )
                binding.layoutTypeIcon.applyTheme()
            }
        }
    }
}
