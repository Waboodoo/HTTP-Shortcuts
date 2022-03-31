package ch.rmy.android.http_shortcuts.activities.editor.authentication

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class AuthenticationEvent : ViewModelEvent() {
    object PromptForClientCertAlias : AuthenticationEvent()
    object OpenCertificateFilePicker : AuthenticationEvent()
}
