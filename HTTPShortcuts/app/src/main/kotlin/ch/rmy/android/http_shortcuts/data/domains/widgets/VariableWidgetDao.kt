package ch.rmy.android.http_shortcuts.data.domains.widgets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.models.VariableWidget

@Dao
interface VariableWidgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(variableWidget: VariableWidget)

    @Query("SELECT * FROM variable_widget WHERE widget_id = :widgetId")
    suspend fun getWidget(widgetId: Int): VariableWidget?

    @Query("SELECT * FROM variable_widget")
    suspend fun getWidgets(): List<VariableWidget>

    @Query("SELECT * FROM variable_widget WHERE widget_id IN (:widgetIds)")
    suspend fun getWidgets(widgetIds: List<Int>): List<VariableWidget>

    @Query("SELECT * FROM variable_widget WHERE variable_id = :variableId")
    suspend fun getWidgetsByVariableId(variableId: GlobalVariableId): List<VariableWidget>

    @Query("DELETE FROM variable_widget WHERE widget_id IN (:widgetIds)")
    suspend fun deleteWidgets(widgetIds: List<Int>)
}
