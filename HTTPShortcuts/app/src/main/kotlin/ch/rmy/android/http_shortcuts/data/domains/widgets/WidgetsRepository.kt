package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.RealmToRoomMigration
import ch.rmy.android.http_shortcuts.data.dao.WidgetDao
import ch.rmy.android.http_shortcuts.data.domains.getShortcutById
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.data.models.WidgetModel
import javax.inject.Inject

class WidgetsRepository
@Inject
constructor(
    realmFactory: RealmFactory,
    database: Database,
) : BaseRepository(realmFactory) {
    private val widgetDaoField = database.widgetDao()

    private suspend fun getWidgetDao(): WidgetDao {
        RealmToRoomMigration.migrationDone.await()
        return widgetDaoField
    }

    suspend fun createWidget(
        widgetId: Int,
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
        iconScale: Float,
    ) {
        getWidgetDao().insert(
            WidgetModel(
                widgetId = widgetId,
                shortcutId = shortcutId,
                showLabel = showLabel,
                showIcon = showIcon,
                labelColor = labelColor,
                iconScale = iconScale,
            ),
        )
    }

    suspend fun getWidgetById(widgetId: Int): Widget? =
        getWidgetDao().getWidget(widgetId)
            ?.let { widget ->
                val shortcut = query { getShortcutById(widget.shortcutId) }.firstOrNull()
                Widget(
                    widgetId = widget.widgetId,
                    shortcut = shortcut,
                    showLabel = widget.showLabel,
                    showIcon = widget.showIcon,
                    labelColor = widget.labelColor,
                    iconScale = widget.iconScale,
                )
            }

    suspend fun getWidgetsByIds(widgetIds: List<Int>): List<Widget> =
        getWidgetDao().getWidgets(widgetIds)
            .map { widget ->
                val shortcut = query { getShortcutById(widget.shortcutId) }.firstOrNull()
                Widget(
                    widgetId = widget.widgetId,
                    shortcut = shortcut,
                    showLabel = widget.showLabel,
                    showIcon = widget.showIcon,
                    labelColor = widget.labelColor,
                    iconScale = widget.iconScale,
                )
            }

    suspend fun getWidgetsByShortcutId(shortcutId: ShortcutId): List<Widget> =
        getWidgetDao().getWidgetsByShortcutId(shortcutId)
            .let { widgets ->
                val shortcut = query { getShortcutById(shortcutId) }.firstOrNull()
                widgets.map { widget ->
                    Widget(
                        widgetId = widget.widgetId,
                        shortcut = shortcut,
                        showLabel = widget.showLabel,
                        showIcon = widget.showIcon,
                        labelColor = widget.labelColor,
                        iconScale = widget.iconScale,
                    )
                }
            }

    suspend fun deleteDeadWidgets() {
        val widgetDao = getWidgetDao()
        widgetDao.getWidgets()
            .mapNotNull { widget ->
                val shortcutExists = query { getShortcutById(widget.shortcutId) }.firstOrNull() != null
                if (shortcutExists) null else widget.widgetId
            }
            .let { deadWidgetIds ->
                widgetDao.deleteWidgets(deadWidgetIds)
            }
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        getWidgetDao().deleteWidgets(widgetIds)
    }
}
