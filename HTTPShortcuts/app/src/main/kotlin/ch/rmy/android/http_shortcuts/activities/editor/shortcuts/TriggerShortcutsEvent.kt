package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder

abstract class TriggerShortcutsEvent : ViewModelEvent() {
    data class ShowShortcutPickerForAdding(val placeholders: List<ShortcutPlaceholder>) : TriggerShortcutsEvent()
    data class ShowRemoveShortcutDialog(val shortcutId: String, val message: Localizable) : TriggerShortcutsEvent()
}
