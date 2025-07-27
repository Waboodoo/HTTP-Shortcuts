package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.ShortcutWidget
import javax.inject.Inject

class ShortcutWidgetsRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun createShortcutWidget(
        widgetId: Int,
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
        iconScale: Float,
    ) = query {
        shortcutWidgetDao().insert(
            ShortcutWidget(
                widgetId = widgetId,
                shortcutId = shortcutId,
                showLabel = showLabel,
                showIcon = showIcon,
                labelColor = labelColor,
                iconScale = iconScale,
            ),
        )
    }

    suspend fun getShortcutWidgetById(widgetId: Int): ShortcutWidget? = query {
        shortcutWidgetDao().getWidget(widgetId)
    }

    suspend fun getShortcutWidgetsByIds(widgetIds: List<Int>): List<ShortcutWidget> = query {
        shortcutWidgetDao().getWidgets(widgetIds)
    }

    suspend fun getShortcutWidgetsByShortcutId(shortcutId: ShortcutId): List<ShortcutWidget> = query {
        shortcutWidgetDao().getWidgetsByShortcutId(shortcutId)
    }

    suspend fun deleteDeadShortcutWidgets() = query {
        val shortcutDao = shortcutDao()
        val widgetDao = shortcutWidgetDao()
        widgetDao.getWidgets()
            .mapNotNull { widget ->
                val shortcutExists = shortcutDao.getShortcutById(widget.shortcutId).firstOrNull() != null
                if (shortcutExists) null else widget.widgetId
            }
            .let { deadWidgetIds ->
                widgetDao.deleteWidgets(deadWidgetIds)
            }
    }

    suspend fun deleteShortcutWidgets(widgetIds: List<Int>) = query {
        shortcutWidgetDao().deleteWidgets(widgetIds)
    }
}
