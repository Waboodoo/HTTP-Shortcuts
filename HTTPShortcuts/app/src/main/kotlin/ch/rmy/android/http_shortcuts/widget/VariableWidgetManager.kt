package ch.rmy.android.http_shortcuts.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.core.util.TypedValueCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.domains.widgets.VariableWidgetsRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.VariableWidget
import javax.inject.Inject

class VariableWidgetManager
@Inject
constructor(
    private val variableWidgetsRepository: VariableWidgetsRepository,
    private val globalVariableRepository: GlobalVariableRepository,
) {
    suspend fun createOrUpdateWidget(
        widgetId: Int,
        globalVariableId: GlobalVariableId,
        fontSize: Int,
        title: String,
    ) {
        variableWidgetsRepository.createOrUpdateVariableWidget(widgetId, globalVariableId, fontSize, title)
    }

    suspend fun updateAllWidgets(context: Context) {
        updateWidgets(context, variableWidgetsRepository.getVariableWidgets())
    }

    suspend fun updateWidgets(context: Context, widgetIds: List<Int>) {
        updateWidgets(context, variableWidgetsRepository.getVariableWidgetsByIds(widgetIds))
    }

    suspend fun updateWidgets(context: Context, variableId: GlobalVariableId) {
        updateWidgets(context, variableWidgetsRepository.getVariableWidgetsByVariableId(variableId))
    }

    @JvmName(name = "_updateWidgets")
    private suspend fun updateWidgets(context: Context, variableWidgets: List<VariableWidget>) {
        variableWidgets.forEach { widget ->
            val variable = try {
                globalVariableRepository.getVariableByKeyOrId(VariableKeyOrId(widget.variableId))
            } catch (_: NoSuchElementException) {
                null
            }
            updateWidget(
                context,
                widget,
                globalVariable = variable,
            )
        }
    }

    private fun updateWidget(context: Context, variableWidget: VariableWidget, globalVariable: GlobalVariable?) {
        RemoteViews(context.packageName, R.layout.variable_widget).also { views ->
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
