package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import ch.rmy.android.framework.extensions.goAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var shortcutWidgetManager: ShortcutWidgetManager

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, widgetIds: IntArray) = goAsync(Dispatchers.Default) {
        shortcutWidgetManager.updateWidgets(widgetIds.toList())
    }

    override fun onDeleted(context: Context, widgetIds: IntArray) = goAsync(Dispatchers.Default) {
        shortcutWidgetManager.deleteWidgets(widgetIds.toList())
    }
}
