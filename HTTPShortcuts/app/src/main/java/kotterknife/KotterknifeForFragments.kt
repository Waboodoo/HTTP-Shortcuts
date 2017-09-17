package kotterknife

import android.view.View
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/*
    This file addresses a problem that arises with fragments. Their life cycle is different from
    normal views or activities in the sense that their UI may be destroyed but the fragment continues
    to exist and its UI may be recreated. This means we can not store view bindings in fragments,
    as they may point to the old, destroyed views instead of the new, recreated ones. Therefore,
    we do not store any view bindings for fragments and instead always look up the actual view.
    A better approach might be to store the bindings and then somehow invalidate them when the
    fragment's UI is destroyed.

    See https://github.com/JakeWharton/kotterknife/issues/5
 */

fun <V : View> BaseFragment.bindView(id: Int)
        : ReadOnlyProperty<BaseFragment, V> = required(id, viewFinder)

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(id: Int, finder: T.(Int) -> View?)
        = LazyNoCache { t: T, desc -> t.finder(id) as V? ?: viewNotFound(id, desc) }

private val BaseFragment.viewFinder: BaseFragment.(Int) -> View?
    get() = { view?.findViewById(it) }

private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing =
        throw IllegalStateException("View ID $id for '${desc.name}' not found.")

private class LazyNoCache<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {

    override fun getValue(thisRef: T, property: KProperty<*>) = initializer(thisRef, property)

}