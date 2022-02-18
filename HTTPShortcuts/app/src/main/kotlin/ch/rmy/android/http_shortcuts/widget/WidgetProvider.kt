package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class WidgetProvider : AppWidgetProvider() {

    private val widgetManager = WidgetManager()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
        widgetManager.updateWidgets(context, widgetIds.toList())
            .blockingAwait() // TODO: Handle with a worker instead of blocking
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) {
        widgetManager.deleteWidgets(widgetIds.toList())
            .blockingAwait() // TODO: Handle with a worker instead of blocking
    }
}
