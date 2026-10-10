package ch.rmy.android.framework.extensions

import androidx.annotation.CheckResult

@CheckResult
fun <T> List<T>.isSubSequenceOf(other: List<T>): Boolean {
    val otherIterator = other.iterator()
    forEach { item ->
        while (true) {
            if (!otherIterator.hasNext()) {
                return false
            }
            val otherItem = otherIterator.next()
            if (otherItem == item) {
                break
            }
        }
    }
    return true
}

@CheckResult
fun <T, ID : Any> List<T>.swapped(id1: ID, id2: ID, getId: T.() -> ID?): List<T> {
    val oldPosition = indexOfFirstOrNull { it.getId() == id1 } ?: return this
    val newPosition = indexOfFirstOrNull { it.getId() == id2 } ?: return this
    return move(oldPosition, newPosition)
}

@CheckResult
fun <T> List<T>.move(oldPosition: Int, newPosition: Int): List<T> =
    toMutableList()
        .also { list ->
            list.add(newPosition, list.removeAt(oldPosition))
        }

fun <T> MutableCollection<T>.addOrRemove(item: T) {
    addOrRemove(item, item !in this)
}

fun <T> MutableCollection<T>.addOrRemove(item: T, add: Boolean) {
    if (add) {
        add(item)
    } else {
        remove(item)
    }
}

@CheckResult
inline fun <T> Collection<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? =
    indexOfFirst(predicate).takeUnless { it == -1 }

@OptIn(ExperimentalStdlibApi::class)
inline fun <T> Collection<T>.hasDuplicatesBy(getKey: (T) -> Any?): Boolean =
    !isEmpty() && !allDistinctBy(getKey)

@CheckResult
fun <T> Map<String, T>.getCaseInsensitive(key: String): T? =
    entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value

@CheckResult
fun <T> T.takeUnlessEmpty(): T? where T : Collection<*> =
    takeUnless { it.isEmpty() }
