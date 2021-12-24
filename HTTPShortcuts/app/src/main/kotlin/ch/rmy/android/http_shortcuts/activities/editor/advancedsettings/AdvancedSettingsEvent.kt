package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import kotlin.time.Duration

abstract class AdvancedSettingsEvent : ViewModelEvent() {
    class ShowTimeoutDialog(val timeout: Duration, val getLabel: (Duration) -> Localizable) : AdvancedSettingsEvent()
    object ShowClientCertDialog : AdvancedSettingsEvent()
}
