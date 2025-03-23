package ch.rmy.android.framework.extensions

import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

fun <T : RealmObject, ID : Any> RealmList<T>.swap(id1: ID, id2: ID, getId: T.() -> ID) {
    val oldPosition = indexOfFirstOrNull { it.getId() == id1 } ?: return
    val newPosition = indexOfFirstOrNull { it.getId() == id2 } ?: return

    add(newPosition, removeAt(oldPosition))
}
