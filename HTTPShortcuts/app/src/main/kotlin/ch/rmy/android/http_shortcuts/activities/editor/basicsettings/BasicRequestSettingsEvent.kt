package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

abstract class BasicRequestSettingsEvent : ViewModelEvent() {
    data class InsertVariablePlaceholder(val variablePlaceholder: VariablePlaceholder) : BasicRequestSettingsEvent()
}
