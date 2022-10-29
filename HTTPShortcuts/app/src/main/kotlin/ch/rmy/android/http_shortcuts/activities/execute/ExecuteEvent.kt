package ch.rmy.android.http_shortcuts.activities.execute

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ExecuteEvent : ViewModelEvent() {
    object ShowProgress : ExecuteEvent()
    object HideProgress : ExecuteEvent()
}
