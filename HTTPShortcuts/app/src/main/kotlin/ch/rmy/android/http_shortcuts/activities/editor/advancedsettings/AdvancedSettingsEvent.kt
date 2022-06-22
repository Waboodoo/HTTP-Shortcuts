package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

abstract class AdvancedSettingsEvent : ViewModelEvent() {
    data class InsertVariablePlaceholder(val variablePlaceholder: VariablePlaceholder) : AdvancedSettingsEvent()
}
