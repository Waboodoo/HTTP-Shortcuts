package ch.rmy.android.http_shortcuts.activities.editor.response

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ResponseEvent : ViewModelEvent() {
    object PickDirectory : ResponseEvent()
}
