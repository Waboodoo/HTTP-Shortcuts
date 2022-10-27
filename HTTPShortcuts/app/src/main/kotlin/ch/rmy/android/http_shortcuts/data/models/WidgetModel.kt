package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(name = "Widget")
open class WidgetModel(
    @PrimaryKey
    var widgetId: Int = 0,
    var shortcut: ShortcutModel? = null,
    var labelColor: String? = null,
    var showLabel: Boolean = true,
) : RealmModel {

    companion object {
        const val FIELD_WIDGET_ID = "widgetId"
        const val FIELD_SHORTCUT = "shortcut"
    }
}
