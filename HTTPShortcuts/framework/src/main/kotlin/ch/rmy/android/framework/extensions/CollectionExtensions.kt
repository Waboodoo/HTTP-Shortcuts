package ch.rmy.android.framework.extensions

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
