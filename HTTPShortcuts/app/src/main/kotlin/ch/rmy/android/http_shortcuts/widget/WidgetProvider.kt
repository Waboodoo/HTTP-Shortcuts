package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import javax.inject.Inject

class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetManager: WidgetManager

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
        context.getApplicationComponent().inject(this)
        widgetManager.updateWidgets(context, widgetIds.toList())
            .blockingAwait() // TODO: Handle with a worker instead of blocking
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) {
        context.getApplicationComponent().inject(this)
        widgetManager.deleteWidgets(widgetIds.toList())
            .blockingAwait() // TODO: Handle with a worker instead of blocking
    }
}
