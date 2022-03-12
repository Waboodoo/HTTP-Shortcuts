package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut

abstract class MainEvent : ViewModelEvent() {
    object ScheduleExecutions : MainEvent()

    data class UpdateLauncherShortcuts(val shortcuts: List<LauncherShortcut>) : MainEvent()
}
