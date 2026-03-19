package ch.rmy.android.http_shortcuts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.RemoteViews
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.widgets.ShortcutWidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.ShortcutWidget
import ch.rmy.android.http_shortcuts.extensions.labelColorInt
import ch.rmy.android.http_shortcuts.utils.IconUtil
import javax.inject.Inject

class ShortcutWidgetManager
@Inject
constructor(
    private val context: Context,
    private val shortcutWidgetsRepository: ShortcutWidgetsRepository,
    private val shortcutRepository: ShortcutRepository,
) {
    suspend fun updateAllWidgets() {
        updateWidgets(shortcutWidgetsRepository.getShortcutWidgets())
    }

    suspend fun updateWidgets(widgetIds: List<Int>) {
        updateWidgets(shortcutWidgetsRepository.getShortcutWidgetsByIds(widgetIds))
    }

    suspend fun updateWidgets(shortcutId: ShortcutId) {
        updateWidgets(shortcutWidgetsRepository.getShortcutWidgetsByShortcutId(shortcutId))
    }

    @JvmName(name = "_updateWidgets")
    private suspend fun updateWidgets(shortcutWidgets: List<ShortcutWidget>) {
        shortcutWidgets.forEach { widget ->
            try {
                updateWidget(
                    widget,
                    shortcut = shortcutRepository.getShortcutById(widget.shortcutId),
                )
            } catch (_: NoSuchElementException) {
                // skip the widget
            }
        }
    }

    private fun updateWidget(shortcutWidget: ShortcutWidget, shortcut: Shortcut) {
        RemoteViews(context.packageName, R.layout.shortcut_widget).also { views ->
            views.setOnClickPendingIntent(
                R.id.widget_base,
                ExecuteActivity.IntentBuilder(shortcut.id)
                    .trigger(ShortcutTriggerType.WIDGET)
                    .build(context)
                    .let { intent ->
                        PendingIntent.getActivity(context, shortcutWidget.widgetId, intent, PendingIntent.FLAG_IMMUTABLE)
                    },
            )
            if (shortcutWidget.showLabel) {
                views.setViewVisibility(R.id.widget_label, View.VISIBLE)
                views.setTextViewText(R.id.widget_label, shortcut.name)
                views.setTextColor(R.id.widget_label, shortcutWidget.labelColorInt())
                views.setTextViewTextSize(R.id.widget_label, COMPLEX_UNIT_SP, if (shortcut.name.length < 20) 18f else 12f)
            } else {
                views.setViewVisibility(R.id.widget_label, View.GONE)
            }
            if (shortcutWidget.showIcon) {
                if (shortcutWidget.showLabel) {
                    views.setInt(R.id.widget_label, "setLines", 2)
                }
                views.setImageViewIcon(R.id.widget_icon, IconUtil.getIcon(context, shortcut.icon, adaptive = false))
                views.setFloat(R.id.widget_icon, "setScaleX", shortcutWidget.iconScale)
                views.setFloat(R.id.widget_icon, "setScaleY", shortcutWidget.iconScale)
            } else {
                views.setInt(R.id.widget_label, "setMaxLines", 4)
                views.setViewVisibility(R.id.widget_icon, View.GONE)
            }

            AppWidgetManager.getInstance(context)
                .updateAppWidget(shortcutWidget.widgetId, views)
        }
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        shortcutWidgetsRepository.deleteDeadShortcutWidgets()
        shortcutWidgetsRepository.deleteShortcutWidgets(widgetIds)
    }
}
