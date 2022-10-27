package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getDeadWidgets
import ch.rmy.android.http_shortcuts.data.domains.getShortcutById
import ch.rmy.android.http_shortcuts.data.domains.getWidgetsByIds
import ch.rmy.android.http_shortcuts.data.domains.getWidgetsForShortcut
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.WidgetModel
import io.realm.kotlin.deleteFromRealm
import javax.inject.Inject

class WidgetsRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun createWidget(widgetId: Int, shortcutId: ShortcutId, showLabel: Boolean, labelColor: String?) {
        commitTransaction {
            copyOrUpdate(
                WidgetModel(
                    widgetId = widgetId,
                    shortcut = getShortcutById(shortcutId).findFirst(),
                    showLabel = showLabel,
                    labelColor = labelColor,
                )
            )
        }
    }

    suspend fun getWidgetsByIds(widgetIds: List<Int>): List<WidgetModel> =
        query {
            getWidgetsByIds(widgetIds)
        }

    suspend fun getWidgetsByShortcutId(shortcutId: ShortcutId): List<WidgetModel> =
        query {
            getWidgetsForShortcut(shortcutId)
        }

    suspend fun deleteDeadWidgets() {
        commitTransaction {
            getDeadWidgets()
                .findAll()
                .forEach { widget ->
                    widget.deleteFromRealm()
                }
        }
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        commitTransaction {
            getWidgetsByIds(widgetIds)
                .findAll()
                .deleteAllFromRealm()
        }
    }
}
