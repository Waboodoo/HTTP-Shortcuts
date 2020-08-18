package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Widget(
    @PrimaryKey
    var widgetId: Int = 0,
    var shortcut: Shortcut? = null,
    var labelColor: String? = null,
    var showLabel: Boolean = true,
) : RealmObject() {

    companion object {
        const val FIELD_WIDGET_ID = "widgetId"
        const val FIELD_SHORTCUT = "shortcut"
    }

}