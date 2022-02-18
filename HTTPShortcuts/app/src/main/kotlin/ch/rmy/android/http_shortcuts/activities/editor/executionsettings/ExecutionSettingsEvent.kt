package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import kotlin.time.Duration

abstract class ExecutionSettingsEvent : ViewModelEvent() {
    class ShowDelayDialog(val delay: Duration, val getLabel: (Duration) -> Localizable) : ExecutionSettingsEvent()
}
