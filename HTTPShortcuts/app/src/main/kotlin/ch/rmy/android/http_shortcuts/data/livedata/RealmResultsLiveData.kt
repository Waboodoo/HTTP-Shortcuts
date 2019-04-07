package ch.rmy.android.http_shortcuts.data.livedata

import io.realm.RealmChangeListener
import io.realm.RealmResults

class RealmResultsLiveData<T>(private val data: RealmResults<T>) : ListLiveData<T>() {

    private val listener = RealmChangeListener<RealmResults<T>> {
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