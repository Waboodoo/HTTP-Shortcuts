package ch.rmy.android.framework.extensions

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

fun <T> T.takeUnlessEmpty(): T? where T : Collection<*> =
    takeUnless { it.isEmpty() }

fun <T> Array<T>.takeUnlessEmpty(): Array<T>? =
    takeUnless { it.isEmpty() }

inline fun <T> Collection<T>.hasDuplicatesBy(getKey: (T) -> Any?): Boolean =
    distinctBy(getKey).size != size

fun Boolean.trueOrNull(): Boolean? =
    if (this) true else null

fun Boolean.falseOrNull(): Boolean? =
    if (!this) false else null
