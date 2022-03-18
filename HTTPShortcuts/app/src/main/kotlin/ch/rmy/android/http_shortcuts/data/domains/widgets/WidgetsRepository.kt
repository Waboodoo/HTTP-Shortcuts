package ch.rmy.android.http_shortcuts.data.domains.widgets

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getDeadWidgets
import ch.rmy.android.http_shortcuts.data.domains.getShortcutById
import ch.rmy.android.http_shortcuts.data.domains.getWidgetsByIds
import ch.rmy.android.http_shortcuts.data.domains.getWidgetsForShortcut
import ch.rmy.android.http_shortcuts.data.models.WidgetModel
import io.reactivex.Completable
import io.reactivex.Single

class WidgetsRepository : BaseRepository(RealmFactory.getInstance()) {

    fun createWidget(widgetId: Int, shortcutId: String, showLabel: Boolean, labelColor: String?): Completable =
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

    fun getWidgetsByIds(widgetIds: List<Int>): Single<List<WidgetModel>> =
        query {
            getWidgetsByIds(widgetIds)
        }

    fun getWidgetsByShortcutId(shortcutId: String): Single<List<WidgetModel>> =
        query {
            getWidgetsForShortcut(shortcutId)
        }

    fun deleteDeadWidgets() =
        commitTransaction {
            getDeadWidgets()
                .findAll()
                .forEach { widget ->
                    widget.deleteFromRealm()
                }
        }

    fun deleteWidgets(widgetIds: List<Int>): Completable =
        commitTransaction {
            getWidgetsByIds(widgetIds)
                .findAll()
                .deleteAllFromRealm()
        }
}
