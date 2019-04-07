package ch.rmy.android.http_shortcuts.data.livedata

import io.realm.RealmChangeListener
import io.realm.RealmList

class RealmListLiveData<T>(private val data: RealmList<T>) : ListLiveData<T>() {

    private val listener = RealmChangeListener<RealmList<T>> {
        onChange()
    }

    override fun getValue(): List<T> = data.takeIf { it.isLoaded && it.isValid } ?: emptyList()

    override fun onActive() {
        if (data.isValid) {
            data.addChangeListener(listener)
        }
        onChange()
    }

    private fun onChange() {
        value = value
    }

    override fun onInactive() {
        if (data.isValid) {
            data.removeChangeListener(listener)
        }
    }

}