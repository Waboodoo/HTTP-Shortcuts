package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class CodeSnippetPickerEvent : ViewModelEvent() {
    object OpenRingtonePicker : CodeSnippetPickerEvent()
    object OpenTaskerTaskPicker : CodeSnippetPickerEvent()
}
