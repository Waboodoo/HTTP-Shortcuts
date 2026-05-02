package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import ch.rmy.android.framework.extensions.goAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class VariableWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var variableWidgetManager: VariableWidgetManager

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) = goAsync(Dispatchers.Default) {
        variableWidgetManager.updateWidgets(widgetIds.toList())
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) = goAsync(Dispatchers.Default) {
        variableWidgetManager.deleteWidgets(widgetIds.toList())
    }
}
