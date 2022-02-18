package ch.rmy.android.http_shortcuts.activities.editor.headers

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class RequestHeadersEvent : ViewModelEvent() {
    object ShowAddHeaderDialog : RequestHeadersEvent()
    data class ShowEditHeaderDialog(
        val headerId: String,
        val key: String,
        val value: String,
    ) : RequestHeadersEvent()
}
