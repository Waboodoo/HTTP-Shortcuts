package ch.rmy.android.http_shortcuts.utils

object ArrayUtil {

    fun <T> findIndex(items: Array<T>, item: T) =
            items.indices.firstOrNull { equals(items[it], item) } ?: 0

    private fun equals(a: Any?, b: Any?) =
            if (a == null) b == null else a == b

    fun findIndex(items: IntArray, item: Int) =
            items.indices.firstOrNull { items[it] == item } ?: 0

}
