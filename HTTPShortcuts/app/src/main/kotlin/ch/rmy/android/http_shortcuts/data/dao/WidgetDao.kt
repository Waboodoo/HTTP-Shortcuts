package ch.rmy.android.http_shortcuts.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.WidgetModel

@Dao
interface WidgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(widget: WidgetModel)

    @Query("SELECT * FROM widget WHERE widget_id = :widgetId")
    suspend fun getWidget(widgetId: Int): WidgetModel?

    @Query("SELECT * FROM widget")
    suspend fun getWidgets(): List<WidgetModel>

    @Query("SELECT * FROM widget WHERE widget_id IN (:widgetIds)")
    suspend fun getWidgets(widgetIds: List<Int>): List<WidgetModel>

    @Query("SELECT * FROM widget WHERE shortcut_id = :shortcutId")
    suspend fun getWidgetsByShortcutId(shortcutId: ShortcutId): List<WidgetModel>

    @Query("DELETE FROM widget WHERE widget_id IN (:widgetIds)")
    suspend fun deleteWidgets(widgetIds: List<Int>)
}
