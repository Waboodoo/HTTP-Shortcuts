package ch.rmy.android.http_shortcuts.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.Image
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.text.Text

class ShortcutWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            Column {
                Image(
                    provider = ,
                    contentDescription = null,
                )

                Text(
                    text =
                )
            }

        }
    }
}