package ch.rmy.android.http_shortcuts.utils

object ArrayUtil {

    fun findIndex(items: Array<Any>, item: Any): Int {
        return items.indices.firstOrNull { equals(items[it], item) } ?: 0
    }

    private fun equals(a: Any?, b: Any?): Boolean {
        return if (a == null) b == null else a == b
    }

    fun findIndex(items: IntArray, item: Int): Int {
        return items.indices.firstOrNull { items[it] == item } ?: 0
    }

}
