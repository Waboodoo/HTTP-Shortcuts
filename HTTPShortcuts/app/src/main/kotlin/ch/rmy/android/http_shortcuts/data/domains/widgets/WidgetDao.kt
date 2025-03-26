package ch.rmy.android.http_shortcuts.data.domains.widgets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.Widget

@Dao
interface WidgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(widget: Widget)

    @Query("SELECT * FROM widget WHERE widget_id = :widgetId")
    suspend fun getWidget(widgetId: Int): Widget?

    @Query("SELECT * FROM widget")
    suspend fun getWidgets(): List<Widget>

    @Query("SELECT * FROM widget WHERE widget_id IN (:widgetIds)")
    suspend fun getWidgets(widgetIds: List<Int>): List<Widget>

    @Query("SELECT * FROM widget WHERE shortcut_id = :shortcutId")
    suspend fun getWidgetsByShortcutId(shortcutId: ShortcutId): List<Widget>

    @Query("DELETE FROM widget WHERE widget_id IN (:widgetIds)")
    suspend fun deleteWidgets(widgetIds: List<Int>)
}
