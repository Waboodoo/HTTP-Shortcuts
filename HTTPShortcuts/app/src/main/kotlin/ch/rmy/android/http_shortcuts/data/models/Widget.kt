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
        iconScale: Float = 1f,
    ) : this() {
        this.widgetId = widgetId
        this.shortcut = shortcut
        this.labelColor = labelColor
        this.showLabel = showLabel
        this.showIcon = showIcon
        this.iconScale = iconScale
    }

    @PrimaryKey
    var widgetId: Int = 0
    var shortcut: Shortcut? = null
    var labelColor: String? = null
    var showLabel: Boolean = true
    var showIcon: Boolean = true
    var iconScale: Float = 1f

    companion object {
        const val FIELD_WIDGET_ID = "widgetId"
        const val FIELD_SHORTCUT = "shortcut"
    }
}
