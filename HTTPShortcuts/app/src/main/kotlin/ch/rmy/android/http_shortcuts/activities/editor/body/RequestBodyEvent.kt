package ch.rmy.android.http_shortcuts.activities.editor.body

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class RequestBodyEvent : ViewModelEvent() {
    object ShowAddParameterTypeSelectionDialog : RequestBodyEvent()

    object ShowAddParameterForStringDialog : RequestBodyEvent()

    data class ShowAddParameterForFileDialog(val multiple: Boolean) : RequestBodyEvent()

    data class ShowEditParameterForStringDialog(
        val parameterId: String,
        val key: String,
        val value: String,
    ) : RequestBodyEvent()

    data class ShowEditParameterForFileDialog(
        val parameterId: String,
        val key: String,
        val showFileNameOption: Boolean,
        val fileName: String,
    ) : RequestBodyEvent()
}
