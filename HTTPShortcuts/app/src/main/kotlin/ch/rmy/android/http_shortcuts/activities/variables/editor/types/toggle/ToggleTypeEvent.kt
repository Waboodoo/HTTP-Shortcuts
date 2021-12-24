package ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ToggleTypeEvent : ViewModelEvent() {
    object ShowAddDialog : ToggleTypeEvent()
    data class ShowEditDialog(
        val optionId: String,
        val value: String,
    ) : ToggleTypeEvent()
}
