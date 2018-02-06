package ch.rmy.android.http_shortcuts.adapters

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ListFragment
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.utils.SelectionMode
import java.util.*

class CategoryPagerAdapter(private val fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val fragments = ArrayList<ListFragment>()
    private val names = ArrayList<String>()

    fun setCategories(categories: List<Category>, selectionMode: SelectionMode) {
        (fragments.size until categories.size)
                .map { fragmentManager.findFragmentByTag(makeFragmentName(it)) }
                .forEach {
                    if (it is ListFragment) {
                        fragments.add(it)
                    } else {
                        fragments.add(ListFragment())
                    }
                }

        while (fragments.size > categories.size) {
            fragments.removeAt(fragments.size - 1)
        }

        names.clear()
        fragments.forEachIndexed { i, fragment ->
            val category = categories[i]
            fragment.categoryId = category.id
            fragment.selectionMode = selectionMode
            names.add(category.name)
        }
        notifyDataSetChanged()
    }

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size

    override fun getPageTitle(position: Int) = names[position]

    private fun makeFragmentName(position: Int) = "android:switcher:" + R.id.view_pager + ":" + position

}