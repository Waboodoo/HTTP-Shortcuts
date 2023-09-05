package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class MainEvent : ViewModelEvent() {
    object Restart : MainEvent()
}
