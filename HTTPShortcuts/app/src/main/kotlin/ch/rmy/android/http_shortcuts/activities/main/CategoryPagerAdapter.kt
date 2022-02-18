package ch.rmy.android.http_shortcuts.activities.main

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode

class CategoryPagerAdapter(
    private val fragmentManager: FragmentManager,
) : FragmentPagerAdapter(
    fragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    private var fragments: List<Pair<String, ShortcutListFragment>> = emptyList()

    private var previousCategories: List<CategoryTabItem>? = null

    fun setCategories(categories: List<CategoryTabItem>, selectionMode: SelectionMode) {
        if (categories == previousCategories) {
            return
        }
        previousCategories = categories
        fragments = categories
            .mapIndexed { index, category ->
                val fragment = fragmentManager.findFragmentByTag(makeFragmentName(index))
                    ?.let { it as? ShortcutListFragment }
                    ?.takeIf { it.categoryId == category.categoryId && it.layoutType == category.layoutType && it.selectionMode == selectionMode }
                    ?: ShortcutListFragment.create(category.categoryId, category.layoutType, selectionMode)
                category.name to fragment
            }
        notifyDataSetChanged()
    }

    override fun getItem(position: Int) = fragments[position].second

    override fun getCount() = fragments.size

    override fun getPageTitle(position: Int) = fragments[position].first

    private fun makeFragmentName(position: Int) = "android:switcher:" + R.id.view_pager + ":" + position
}
