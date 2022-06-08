package ch.rmy.android.http_shortcuts.activities.execute

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel

abstract class ExecuteEvent : ViewModelEvent() {
    data class Execute(
        val shortcut: ShortcutModel,
        val globalCode: String,
    ) : ExecuteEvent()
}
