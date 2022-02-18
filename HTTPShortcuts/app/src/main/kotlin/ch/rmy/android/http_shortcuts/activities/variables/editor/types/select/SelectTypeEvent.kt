package ch.rmy.android.http_shortcuts.activities.variables.editor.types.select

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class SelectTypeEvent : ViewModelEvent() {
    object ShowAddDialog : SelectTypeEvent()
    data class ShowEditDialog(
        val optionId: String,
        val label: String,
        val value: String,
    ) : SelectTypeEvent()
}
