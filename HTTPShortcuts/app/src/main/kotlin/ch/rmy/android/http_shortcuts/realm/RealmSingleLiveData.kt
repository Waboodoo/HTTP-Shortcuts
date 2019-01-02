package ch.rmy.android.http_shortcuts.realm

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmObject

class RealmSingleLiveData<T : RealmObject>(private val data: T) : LiveData<T?>() {

    private val listener = RealmChangeListener<T> {
        onChange()
    }

    override fun getValue(): T? = data.takeIf { it.isLoaded && it.isValid }

    override fun onActive() {
        data.addChangeListener(listener)
        onChange()
    }

    private fun onChange() {
        value = value
    }

    override fun onInactive() {
        data.removeChangeListener(listener)
    }

}