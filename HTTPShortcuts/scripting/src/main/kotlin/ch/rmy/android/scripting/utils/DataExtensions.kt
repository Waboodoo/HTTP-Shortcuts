package ch.rmy.android.scripting.utils

internal fun Any?.withoutCycles(references: MutableList<Any?> = mutableListOf<Any?>()): Any? =
    if (this is Map<*, *>) {
        (this as Map<Any?, Any?>).withoutCycles(references)
    } else if (this is List<*>) {
        this.withoutCycles(references)
    } else {
        this
    }

internal fun Map<Any?, Any?>.withoutCycles(references: MutableList<Any?> = mutableListOf<Any?>()): Map<Any?, Any?> {
    val map = this
    references.add(this)
    return buildMap {
        map.forEach { key, value ->
            if (references.none { it === value }) {
                references.add(value)
                put(key, value.withoutCycles(references))
            } else {
                put(key, null)
            }
        }
    }
}

internal fun List<Any?>.withoutCycles(references: MutableList<Any?> = mutableListOf<Any?>()): List<Any?> {
    val list = this
    references.add(this)
    return buildList {
        list.forEach { item ->
            if (references.none { it === item }) {
                references.add(item)
                add(item.withoutCycles(references))
            } else {
                add(null)
            }
        }
    }
}
