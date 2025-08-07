package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import ch.rmy.android.framework.extensions.createIntent

object WidgetsUtil {
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
