package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId

@Entity(tableName = "variable_widget")
data class VariableWidget(
    @PrimaryKey
    @ColumnInfo(name = "widget_id")
    val widgetId: Int,
    @ColumnInfo(name = "variable_id", index = true)
    val variableId: GlobalVariableId,
    @ColumnInfo(name = "font_size", defaultValue = "22")
    val fontSize: Int,
    @ColumnInfo(name = "Title", defaultValue = "")
    val title: String,
    @ColumnInfo(name = "shortcut_id")
    val shortcutId: ShortcutId? = null,
)
