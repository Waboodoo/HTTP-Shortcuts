package ch.rmy.android.http_shortcuts.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Widget() : RealmObject {

    constructor(
        widgetId: Int = 0,
        shortcut: Shortcut? = null,
        labelColor: String? = null,
        showLabel: Boolean = true,
        showIcon: Boolean = true,
    ) : this() {
        this.widgetId = widgetId
        this.shortcut = shortcut
        this.labelColor = labelColor
        this.showLabel = showLabel
        this.showIcon = showIcon
    }

    @PrimaryKey
    var widgetId: Int = 0
    var shortcut: Shortcut? = null
    var labelColor: String? = null
    var showLabel: Boolean = true
    var showIcon: Boolean = true

    companion object {
        const val FIELD_WIDGET_ID = "widgetId"
        const val FIELD_SHORTCUT = "shortcut"
    }
}
