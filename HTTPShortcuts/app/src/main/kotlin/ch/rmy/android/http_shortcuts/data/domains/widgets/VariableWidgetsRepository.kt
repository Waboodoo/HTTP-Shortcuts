package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.models.VariableWidget
import javax.inject.Inject

class VariableWidgetsRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun createOrUpdateVariableWidget(
        widgetId: Int,
        variableId: GlobalVariableId,
        fontSize: Int,
    ) = query {
        variableWidgetDao().insertOrReplace(
            VariableWidget(
                widgetId = widgetId,
                variableId = variableId,
                fontSize = fontSize,
            ),
        )
    }

    suspend fun getVariableWidgets(): List<VariableWidget> = query {
        variableWidgetDao().getWidgets()
    }

    suspend fun getVariableWidgetById(widgetId: Int): VariableWidget? = query {
        variableWidgetDao().getWidget(widgetId)
    }

    suspend fun getVariableWidgetsByIds(widgetIds: List<Int>): List<VariableWidget> = query {
        variableWidgetDao().getWidgets(widgetIds)
    }

    suspend fun getVariableWidgetsByVariableId(variableId: GlobalVariableId): List<VariableWidget> = query {
        variableWidgetDao().getWidgetsByVariableId(variableId)
    }

    suspend fun deleteDeadVariableWidgets() = query {
        val globalVariableDao = globalVariableDao()
        val widgetDao = variableWidgetDao()
        widgetDao.getWidgets()
            .mapNotNull { widget ->
                val variableExists = globalVariableDao.getVariableById(widget.variableId).firstOrNull() != null
                if (variableExists) null else widget.widgetId
            }
            .let { deadWidgetIds ->
                widgetDao.deleteWidgets(deadWidgetIds)
            }
    }

    suspend fun deleteVariableWidgets(widgetIds: List<Int>) = query {
        variableWidgetDao().deleteWidgets(widgetIds)
    }
}
