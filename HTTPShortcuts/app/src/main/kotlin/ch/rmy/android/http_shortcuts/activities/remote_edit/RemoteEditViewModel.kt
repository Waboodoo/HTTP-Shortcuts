package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData

class RemoteEditViewModel(application: Application) : AndroidViewModel(application) {

    private val eventList = mutableListOf<String>()

    val events = object : ListLiveData<String>() {
        override fun getValue(): List<String> = eventList
    }

    fun onApiEvent(event: String) {
        eventList.add(event)
        if (eventList.size > MAX_EVENTS) {
            eventList.dropLast(1)
        }
        events.onChange()
    }

    companion object {
        private const val MAX_EVENTS = 100
    }

}