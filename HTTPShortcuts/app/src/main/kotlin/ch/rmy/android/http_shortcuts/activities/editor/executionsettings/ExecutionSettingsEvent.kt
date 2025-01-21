package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ExecutionSettingsEvent : ViewModelEvent() {
    object RequestNotificationPermission : ExecutionSettingsEvent()
}
