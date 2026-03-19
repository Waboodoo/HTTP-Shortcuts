package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VariableWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetUpdateWorkerStarter: WidgetUpdateWorker.Starter

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
        widgetUpdateWorkerStarter.updateVariableWidgets(widgetIds)
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) {
        widgetUpdateWorkerStarter.deleteVariableWidgets(widgetIds)
    }
}
