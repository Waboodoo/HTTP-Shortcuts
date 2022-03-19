package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class AdvancedSettingsEvent : ViewModelEvent() {
    object PromptForClientCertAlias : AdvancedSettingsEvent()
    object OpenCertificateFilePicker : AdvancedSettingsEvent()
}
