package ch.rmy.android.http_shortcuts.data.livedata

import androidx.lifecycle.LiveData

abstract class ListLiveData<T> : LiveData<List<T>>(), Collection<T> {

    operator fun get(index: Int) = value?.get(index)

    override val size: Int
        get() = value?.size ?: 0

    override fun contains(element: T): Boolean =
        value?.contains(element) == true

    override fun containsAll(elements: Collection<T>): Boolean =
        value?.containsAll(elements) == true

    override fun isEmpty(): Boolean =
        value?.isEmpty() != false

    override fun iterator() = object : Iterator<T> {

        var offset = -1

        override fun hasNext() = offset + 1 < size

        override fun next(): T {
            offset++
            return get(offset)!!
        }
    }

}