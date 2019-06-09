package ch.rmy.android.http_shortcuts.data.livedata

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmObject

class RealmSingleLiveData<T : RealmObject>(private val data: T) : LiveData<T?>() { // TODO: Make this mutable

    private val listener = RealmChangeListener<T> {
        onChange()
    }

    override fun getValue(): T? = data.takeIf { it.isValid && it.isLoaded }

    override fun onActive() {
        if (data.isValid || !data.isLoaded) {
            data.addChangeListener(listener)
        }
        onChange()
    }

    private fun onChange() {
        value = value
    }

    override fun onInactive() {
        if (data.isValid || !data.isLoaded) {
            data.removeChangeListener(listener)
        }
    }

}