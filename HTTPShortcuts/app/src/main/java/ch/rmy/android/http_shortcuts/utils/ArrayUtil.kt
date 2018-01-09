package ch.rmy.android.http_shortcuts.utils

object ArrayUtil {

    fun <T> findIndex(items: Array<T>, item: T) =
            items.indices.firstOrNull { items[it] == item } ?: 0

    fun findIndex(items: IntArray, item: Int) =
            items.indices.firstOrNull { items[it] == item } ?: 0

}
