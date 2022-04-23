package ch.rmy.android.http_shortcuts.activities.categories.editor

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class CategoryEditorEvent : ViewModelEvent() {
    object RequestFilePermissionsIfNeeded : CategoryEditorEvent()
}
