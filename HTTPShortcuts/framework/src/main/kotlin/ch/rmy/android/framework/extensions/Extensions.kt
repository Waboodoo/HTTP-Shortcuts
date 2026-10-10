package ch.rmy.android.framework.extensions

import androidx.annotation.CheckResult

@CheckResult
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

@CheckResult
fun <T> Array<T>.takeUnlessEmpty(): Array<T>? =
    takeUnless { it.isEmpty() }

@CheckResult
fun Boolean.trueOrNull(): Boolean? =
    if (this) true else null

@CheckResult
fun Boolean.falseOrNull(): Boolean? =
    if (!this) false else null
