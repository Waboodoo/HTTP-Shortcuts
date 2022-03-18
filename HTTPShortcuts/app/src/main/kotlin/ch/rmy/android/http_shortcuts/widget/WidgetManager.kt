package ch.rmy.android.http_shortcuts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.widgets.WidgetsRepository
import ch.rmy.android.http_shortcuts.data.models.WidgetModel
import ch.rmy.android.http_shortcuts.utils.IconUtil
import io.reactivex.Completable

class WidgetManager {

    private val widgetsRepository = WidgetsRepository()

    @CheckResult
    fun createWidget(widgetId: Int, shortcutId: String, showLabel: Boolean, labelColor: String?) =
        widgetsRepository.createWidget(widgetId, shortcutId, showLabel, labelColor)

    @CheckResult
    fun updateWidgets(context: Context, widgetIds: List<Int>): Completable =
        widgetsRepository.getWidgetsByIds(widgetIds)
            .flatMapCompletable { widgets ->
                Completable.fromAction {
                    widgets.forEach { widget ->
                        updateWidget(context, widget)
                    }
                }
            }

    @CheckResult
    fun updateWidgets(context: Context, shortcutId: String): Completable =
        widgetsRepository.getWidgetsByShortcutId(shortcutId)
            .flatMapCompletable { widgets ->
                Completable.fromAction {
                    widgets.forEach { widget ->
                        updateWidget(context, widget)
                    }
                }
            }

    private fun updateWidget(context: Context, widget: WidgetModel) {
        val shortcut = widget.shortcut ?: return
        RemoteViews(context.packageName, R.layout.widget).also { views ->
            views.setOnClickPendingIntent(
                R.id.widget_base,
                ExecuteActivity.IntentBuilder(shortcut.id)
                    .build(context)
                    .let { intent ->
                        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PendingIntent.FLAG_IMMUTABLE
                        } else 0
                        PendingIntent.getActivity(context, 0, intent, flags)
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

    fun deleteWidgets(widgetIds: List<Int>): Completable =
        widgetsRepository.deleteDeadWidgets()
            .andThen(widgetsRepository.deleteWidgets(widgetIds))

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
