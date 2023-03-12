package ch.rmy.android.http_shortcuts.activities.editor.response

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

abstract class ResponseEvent : ViewModelEvent() {
    data class InsertVariablePlaceholderIntoSuccessMessage(val variablePlaceholder: VariablePlaceholder) : ResponseEvent()
    data class InsertVariablePlaceholderIntoFileName(val variablePlaceholder: VariablePlaceholder) : ResponseEvent()
    object PickDirectory : ResponseEvent()
}
