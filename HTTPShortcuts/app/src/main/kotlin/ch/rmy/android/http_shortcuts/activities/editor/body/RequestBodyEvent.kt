package ch.rmy.android.http_shortcuts.activities.editor.body

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

abstract class RequestBodyEvent : ViewModelEvent() {
    data class InsertVariablePlaceholder(val variablePlaceholder: VariablePlaceholder) : RequestBodyEvent()
}
