package ch.rmy.android.http_shortcuts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.util.TypedValueCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.domains.widgets.VariableWidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.VariableWidget
import javax.inject.Inject

class VariableWidgetManager
@Inject
constructor(
    private val context: Context,
    private val variableWidgetsRepository: VariableWidgetsRepository,
    private val globalVariableRepository: GlobalVariableRepository,
) {
    suspend fun createOrUpdateWidget(
        widgetId: Int,
        globalVariableId: GlobalVariableId,
        fontSize: Int,
        title: String,
        shortcutId: ShortcutId?,
    ) {
        variableWidgetsRepository.createOrUpdateVariableWidget(widgetId, globalVariableId, fontSize, title, shortcutId)
    }

    suspend fun updateAllWidgets() {
        updateWidgets(variableWidgetsRepository.getVariableWidgets())
    }

    suspend fun updateWidgets(widgetIds: List<Int>) {
        updateWidgets(variableWidgetsRepository.getVariableWidgetsByIds(widgetIds))
    }

    suspend fun updateWidgets(variableId: GlobalVariableId) {
        updateWidgets(variableWidgetsRepository.getVariableWidgetsByVariableId(variableId))
    }

    @JvmName(name = "_updateWidgets")
    private suspend fun updateWidgets(variableWidgets: List<VariableWidget>) {
        variableWidgets.forEach { widget ->
            val variable = try {
                globalVariableRepository.getVariableByKeyOrId(VariableKeyOrId(widget.variableId))
            } catch (_: NoSuchElementException) {
                null
            }
            updateWidget(
                widget,
                globalVariable = variable,
            )
        }
    }

    private fun updateWidget(variableWidget: VariableWidget, globalVariable: GlobalVariable?) {
        RemoteViews(context.packageName, R.layout.variable_widget).also { views ->
            val shortcutId = variableWidget.shortcutId
            if (shortcutId != null) {
                views.setOnClickPendingIntent(
                    R.id.widget_base,
                    ExecuteActivity.IntentBuilder(shortcutId)
                        .trigger(ShortcutTriggerType.WIDGET)
                        .build(context)
                        .let { intent ->
                            PendingIntent.getActivity(context, variableWidget.widgetId, intent, PendingIntent.FLAG_IMMUTABLE)
                        },
                )
            } else {
                // There's no way to remove a click action, so instead we just set a dummy intent that won't be handled by anything
                val intent = Intent("http-shortcuts-dummy-action")
                views.setOnClickPendingIntent(
                    R.id.widget_base,
                    PendingIntent.getBroadcast(context, variableWidget.widgetId, intent, PendingIntent.FLAG_IMMUTABLE),
                )
            }

            views.setTextViewText(R.id.widget_text, globalVariable?.value?.ifEmpty { "-" } ?: "???")

            val fontSize = variableWidget.fontSize.toFloat()
            views.setFloat(R.id.widget_text, "setTextSize", fontSize)
            views.setInt(R.id.widget_text, "setLineHeight", TypedValueCompat.dpToPx(fontSize, context.resources.displayMetrics).toInt())

            if (variableWidget.title.isNotEmpty()) {
                views.setViewVisibility(R.id.widget_title, View.VISIBLE)
                views.setTextViewText(R.id.widget_title, variableWidget.title)

                val titleFontSize = fontSize * 0.75f
                views.setFloat(R.id.widget_title, "setTextSize", titleFontSize)
                views.setInt(R.id.widget_title, "setLineHeight", TypedValueCompat.dpToPx(titleFontSize, context.resources.displayMetrics).toInt())
            } else {
                views.setViewVisibility(R.id.widget_title, View.GONE)
            }

            AppWidgetManager.getInstance(context)
                .updateAppWidget(variableWidget.widgetId, views)
        }
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        variableWidgetsRepository.deleteDeadVariableWidgets()
        variableWidgetsRepository.deleteVariableWidgets(widgetIds)
    }
}
