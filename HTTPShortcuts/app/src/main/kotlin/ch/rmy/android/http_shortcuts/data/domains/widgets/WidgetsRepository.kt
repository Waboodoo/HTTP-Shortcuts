package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.Widget
import javax.inject.Inject

class WidgetsRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun createWidget(
        widgetId: Int,
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
        iconScale: Float,
    ) = query {
        widgetDao().insert(
            Widget(
                widgetId = widgetId,
                shortcutId = shortcutId,
                showLabel = showLabel,
                showIcon = showIcon,
                labelColor = labelColor,
                iconScale = iconScale,
            ),
        )
    }

    suspend fun getWidgetById(widgetId: Int): Widget? = query {
        widgetDao().getWidget(widgetId)
    }

    suspend fun getWidgetsByIds(widgetIds: List<Int>): List<Widget> = query {
        widgetDao().getWidgets(widgetIds)
    }

    suspend fun getWidgetsByShortcutId(shortcutId: ShortcutId): List<Widget> = query {
        widgetDao().getWidgetsByShortcutId(shortcutId)
    }

    suspend fun deleteDeadWidgets() = query {
        val shortcutDao = shortcutDao()
        val widgetDao = widgetDao()
        widgetDao.getWidgets()
            .mapNotNull { widget ->
                val shortcutExists = shortcutDao.getShortcutById(widget.shortcutId).firstOrNull() != null
                if (shortcutExists) null else widget.widgetId
            }
            .let { deadWidgetIds ->
                widgetDao.deleteWidgets(deadWidgetIds)
            }
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) = query {
        widgetDao().deleteWidgets(widgetIds)
    }
}
