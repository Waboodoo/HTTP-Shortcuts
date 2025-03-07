package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetManager: WidgetManager

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
        // TODO: Use more appropriate scope, maybe a worker?
        CoroutineScope(Dispatchers.Default).launch {
            widgetManager.updateWidgets(context, widgetIds.toList())
        }
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) {
        // TODO: Use more appropriate scope, maybe a worker?
        CoroutineScope(Dispatchers.Default).launch {
            widgetManager.deleteWidgets(widgetIds.toList())
        }
    }
}
