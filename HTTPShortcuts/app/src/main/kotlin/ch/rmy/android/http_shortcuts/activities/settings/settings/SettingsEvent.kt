package ch.rmy.android.http_shortcuts.activities.settings.settings

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class SettingsEvent : ViewModelEvent() {
    object AddQuickSettingsTile : SettingsEvent()
}
