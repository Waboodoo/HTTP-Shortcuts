package ch.rmy.android.http_shortcuts.activities.response

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class DisplayResponseEvent : ViewModelEvent() {
    data class PickFileForSaving(val mimeType: String?) : DisplayResponseEvent()
    object SuppressAutoFinish : DisplayResponseEvent()
}
