package ch.rmy.android.http_shortcuts.activities.main

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.utils.SelectionMode

class CategoryPagerAdapter(private val fragmentManager: FragmentManager, private val selectionMode: SelectionMode) : FragmentPagerAdapter(fragmentManager) {

    private var fragments: List<Pair<String, ListFragment>> = emptyList()

    fun setCategories(categories: List<Category>) {
        fragments = categories
            .mapIndexed { index, category ->
                val fragment = fragmentManager.findFragmentByTag(makeFragmentName(index))
                    ?.let { it as? ListFragment }
                    ?.takeIf { it.categoryId == category.id }
                    ?: ListFragment.create(category.id, selectionMode)

                category.name to fragment
            }
        notifyDataSetChanged()
    }

    override fun getItem(position: Int) = fragments[position].second

    override fun getCount() = fragments.size

    override fun getPageTitle(position: Int) = fragments[position].first

    private fun makeFragmentName(position: Int) = "android:switcher:" + R.id.view_pager + ":" + position

}