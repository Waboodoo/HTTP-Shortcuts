package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getDeadWidgets
import ch.rmy.android.http_shortcuts.data.domains.getShortcutById
import ch.rmy.android.http_shortcuts.data.domains.getWidgetsByIds
import ch.rmy.android.http_shortcuts.data.domains.getWidgetsForShortcut
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.Widget
import javax.inject.Inject

class WidgetsRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun createWidget(
        widgetId: Int,
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
    ) {
        commitTransaction {
            copyOrUpdate(
                Widget(
                    widgetId = widgetId,
                    shortcut = getShortcutById(shortcutId).findFirst(),
                    showLabel = showLabel,
                    showIcon = showIcon,
                    labelColor = labelColor,
                )
            )
        }
    }

    suspend fun getWidgetsByIds(widgetIds: List<Int>): List<Widget> =
        query {
            getWidgetsByIds(widgetIds)
        }

    suspend fun getWidgetsByShortcutId(shortcutId: ShortcutId): List<Widget> =
        query {
            getWidgetsForShortcut(shortcutId)
        }

    suspend fun deleteDeadWidgets() {
        commitTransaction {
            getDeadWidgets()
                .find()
                .forEach { widget ->
                    widget.delete()
                }
        }
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        commitTransaction {
            getWidgetsByIds(widgetIds).deleteAll()
        }
    }
}
