package ch.rmy.android.http_shortcuts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetsRepository
import ch.rmy.android.http_shortcuts.data.models.WidgetModel
import ch.rmy.android.http_shortcuts.utils.IconUtil
import javax.inject.Inject

class WidgetManager
@Inject
constructor(
    private val widgetsRepository: WidgetsRepository,
) {

    suspend fun createWidget(widgetId: Int, shortcutId: ShortcutId, showLabel: Boolean, labelColor: String?) {
        widgetsRepository.createWidget(widgetId, shortcutId, showLabel, labelColor)
    }

    suspend fun updateWidgets(context: Context, widgetIds: List<Int>) {
        widgetsRepository.getWidgetsByIds(widgetIds)
            .forEach { widget ->
                updateWidget(context, widget)
            }
    }

    suspend fun updateWidgets(context: Context, shortcutId: ShortcutId) {
        widgetsRepository.getWidgetsByShortcutId(shortcutId)
            .forEach { widget ->
                updateWidget(context, widget)
            }
    }

    private fun updateWidget(context: Context, widget: WidgetModel) {
        val shortcut = widget.shortcut ?: return
        RemoteViews(context.packageName, R.layout.widget).also { views ->
            views.setOnClickPendingIntent(
                R.id.widget_base,
                ExecuteActivity.IntentBuilder(shortcut.id)
                    .trigger("widget")
                    .build(context)
                    .let { intent ->
                        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PendingIntent.FLAG_IMMUTABLE
                        } else 0
                        PendingIntent.getActivity(context, widget.widgetId, intent, flags)
                    }
            )
            if (widget.showLabel) {
                views.setViewVisibility(R.id.widget_label, View.VISIBLE)
                views.setTextViewText(R.id.widget_label, shortcut.name)
                views.setTextColor(R.id.widget_label, widget.labelColor?.let(Color::parseColor) ?: Color.WHITE)
            } else {
                views.setViewVisibility(R.id.widget_label, View.GONE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                views.setImageViewIcon(R.id.widget_icon, IconUtil.getIcon(context, shortcut.icon))
            } else {
                views.setImageViewUri(R.id.widget_icon, shortcut.icon.getIconURI(context, external = true))
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
