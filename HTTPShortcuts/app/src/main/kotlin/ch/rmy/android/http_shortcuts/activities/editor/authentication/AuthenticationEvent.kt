package ch.rmy.android.http_shortcuts.activities.editor.authentication

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

abstract class AuthenticationEvent : ViewModelEvent() {
    object PromptForClientCertAlias : AuthenticationEvent()
    object OpenCertificateFilePicker : AuthenticationEvent()
    data class InsertVariablePlaceholderForUsername(val variablePlaceholder: VariablePlaceholder) : AuthenticationEvent()
    data class InsertVariablePlaceholderForPassword(val variablePlaceholder: VariablePlaceholder) : AuthenticationEvent()
    data class InsertVariablePlaceholderForToken(val variablePlaceholder: VariablePlaceholder) : AuthenticationEvent()
}
