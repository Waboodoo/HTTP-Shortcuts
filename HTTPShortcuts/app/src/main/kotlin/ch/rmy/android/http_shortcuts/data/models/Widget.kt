package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Entity(tableName = "widget")
data class Widget(
    @PrimaryKey
    @ColumnInfo(name = "widget_id")
    val widgetId: Int,
    @ColumnInfo(name = "shortcut_id", index = true)
    val shortcutId: ShortcutId,
    @ColumnInfo(name = "label_color")
    val labelColor: String?,
    @ColumnInfo(name = "show_label")
    val showLabel: Boolean,
    @ColumnInfo(name = "show_icon")
    val showIcon: Boolean,
    @ColumnInfo(name = "icon_scale")
    val iconScale: Float,
)
