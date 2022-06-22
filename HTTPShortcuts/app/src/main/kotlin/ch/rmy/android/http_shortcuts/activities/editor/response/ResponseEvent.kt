package ch.rmy.android.http_shortcuts.activities.editor.response

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

abstract class ResponseEvent : ViewModelEvent() {
    data class InsertVariablePlaceholder(val variablePlaceholder: VariablePlaceholder) : ResponseEvent()
}
