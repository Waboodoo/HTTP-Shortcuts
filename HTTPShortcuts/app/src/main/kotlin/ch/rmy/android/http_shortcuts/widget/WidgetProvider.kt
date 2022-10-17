package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetManager: WidgetManager

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
        context.getApplicationComponent().inject(this)
        // TODO: Use more appropriate scope, maybe a worker?
        CoroutineScope(Dispatchers.Default).launch {
            widgetManager.updateWidgets(context, widgetIds.toList())
        }
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) {
        context.getApplicationComponent().inject(this)
        // TODO: Use more appropriate scope, maybe a worker?
        CoroutineScope(Dispatchers.Default).launch {
            widgetManager.deleteWidgets(widgetIds.toList())
        }
    }
}
