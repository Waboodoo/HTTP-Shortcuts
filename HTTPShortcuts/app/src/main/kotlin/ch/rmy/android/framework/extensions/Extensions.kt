package ch.rmy.android.framework.extensions

import androidx.annotation.CheckResult

inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

inline fun <T> T.applyIf(predicate: Boolean, block: T.() -> Unit): T =
    if (predicate) apply(block) else this

inline fun <T, U> T.applyIfNotNull(item: U?, block: T.(U) -> Unit): T =
    if (item != null) apply { block(item) } else this

inline fun <T> T.runIf(predicate: Boolean, block: T.() -> T): T =
    if (predicate) block(this) else this

inline fun <T, U> T.runIfNotNull(item: U?, block: T.(U) -> T): T =
    if (item != null) block(this, item) else this

inline fun <T, U> T.runFor(iterable: Iterable<U>, block: T.(U) -> T): T {
    val iterator = iterable.iterator()
    var item = this
    while (iterator.hasNext()) {
        item = block.invoke(item, iterator.next())
    }
    return item
}

fun <T> Map<String, T>.getCaseInsensitive(key: String): T? =
    entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value

fun <T> T.takeUnlessEmpty(): T? where T : Collection<*> =
    takeUnless { it.isEmpty() }

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

inline fun <T> Collection<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? =
    indexOfFirst(predicate).takeUnless { it == -1 }

inline fun <T> Collection<T>.hasDuplicatesBy(getKey: (T) -> Any?): Boolean =
    distinctBy(getKey).size != size

fun Boolean.trueOrNull(): Boolean? =
    if (this) true else null

fun Boolean.falseOrNull(): Boolean? =
    if (!this) false else null
