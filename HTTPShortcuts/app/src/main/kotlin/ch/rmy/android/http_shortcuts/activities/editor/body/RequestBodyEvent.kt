package ch.rmy.android.http_shortcuts.activities.editor.body

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class RequestBodyEvent : ViewModelEvent() {
    object PickFileForBody : RequestBodyEvent()
    object PickFileForParameter : RequestBodyEvent()
}
