package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

abstract class MainEvent : ViewModelEvent() {
    object OpenCurlImport : MainEvent()

    data class OpenWidgetSettings(val shortcut: ShortcutPlaceholder) : MainEvent()

    data class OpenShortcutEditor(val intentBuilder: ShortcutEditorActivity.IntentBuilder) : MainEvent()

    object OpenCategories : MainEvent()

    object OpenSettings : MainEvent()

    object OpenImportExport : MainEvent()
}
