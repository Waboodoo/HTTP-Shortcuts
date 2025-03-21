package ch.rmy.android.http_shortcuts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.RemoteViews
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.extensions.labelColorInt
import ch.rmy.android.http_shortcuts.utils.IconUtil
import javax.inject.Inject

class WidgetManager
@Inject
constructor(
    private val widgetsRepository: WidgetsRepository,
    private val shortcutRepository: ShortcutRepository,
) {

    suspend fun createWidget(
        widgetId: Int,
        shortcutId: ShortcutId,
        showLabel: Boolean,
        showIcon: Boolean,
        labelColor: String?,
        iconScale: Float,
    ) {
        widgetsRepository.createWidget(widgetId, shortcutId, showLabel, showIcon, labelColor, iconScale)
    }

    suspend fun updateWidgets(context: Context, widgetIds: List<Int>) {
        updateWidgets(context, widgetsRepository.getWidgetsByIds(widgetIds))
    }

    suspend fun updateWidgets(context: Context, shortcutId: ShortcutId) {
        updateWidgets(context, widgetsRepository.getWidgetsByShortcutId(shortcutId))
    }

    @JvmName(name = "_updateWidgets")
    private suspend fun updateWidgets(context: Context, widgets: List<Widget>) {
        widgets.forEach { widget ->
            try {
                updateWidget(
                    context,
                    widget,
                    shortcut = shortcutRepository.getShortcutById(widget.shortcutId),
                )
            } catch (_: NoSuchElementException) {
                // skip the widget
            }
        }
    }

    private fun updateWidget(context: Context, widget: Widget, shortcut: Shortcut) {
        RemoteViews(context.packageName, R.layout.widget).also { views ->
            views.setOnClickPendingIntent(
                R.id.widget_base,
                ExecuteActivity.IntentBuilder(shortcut.id)
                    .trigger(ShortcutTriggerType.WIDGET)
                    .build(context)
                    .let { intent ->
                        PendingIntent.getActivity(context, widget.widgetId, intent, PendingIntent.FLAG_IMMUTABLE)
                    },
            )
            if (widget.showLabel) {
                views.setViewVisibility(R.id.widget_label, View.VISIBLE)
                views.setTextViewText(R.id.widget_label, shortcut.name)
                views.setTextColor(R.id.widget_label, widget.labelColorInt())
                views.setTextViewTextSize(R.id.widget_label, COMPLEX_UNIT_SP, if (shortcut.name.length < 20) 18f else 12f)
            } else {
                views.setViewVisibility(R.id.widget_label, View.GONE)
            }
            if (widget.showIcon) {
                if (widget.showLabel) {
                    views.setInt(R.id.widget_label, "setLines", 2)
                }
                views.setImageViewIcon(R.id.widget_icon, IconUtil.getIcon(context, shortcut.icon, adaptive = false))
                views.setFloat(R.id.widget_icon, "setScaleX", widget.iconScale)
                views.setFloat(R.id.widget_icon, "setScaleY", widget.iconScale)
            } else {
                views.setInt(R.id.widget_label, "setMaxLines", 4)
                views.setViewVisibility(R.id.widget_icon, View.GONE)
            }

            AppWidgetManager.getInstance(context)
                .updateAppWidget(widget.widgetId, views)
        }
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        widgetsRepository.deleteDeadWidgets()
        widgetsRepository.deleteWidgets(widgetIds)
    }

    companion object {

        fun getIntent(widgetId: Int) =
            createIntent {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

        fun getWidgetIdFromIntent(intent: Intent): Int? =
            intent.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            )
                ?.takeUnless {
                    it == AppWidgetManager.INVALID_APPWIDGET_ID
                }
    }
}
