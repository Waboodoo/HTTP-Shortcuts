package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProvider : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var widgetManager: WidgetManager

    override val glanceAppWidget: GlanceAppWidget = ShortcutWidget()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, widgetIds)
        // TODO: Use more appropriate scope, maybe a worker?
        CoroutineScope(Dispatchers.Default).launch {
            widgetManager.updateWidgets(context, widgetIds.toList())
        }
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) {
        super.onDeleted(context, widgetIds)
        // TODO: Use more appropriate scope, maybe a worker?
        CoroutineScope(Dispatchers.Default).launch {
            widgetManager.deleteWidgets(widgetIds.toList())
        }
    }
}
