package ch.rmy.android.http_shortcuts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.FrameLayout
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
import ch.rmy.android.http_shortcuts.data.enums.WidgetBackgroundType
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
        background: WidgetBackgroundType?,
        shortcutId: ShortcutId?,
    ) {
        variableWidgetsRepository.createOrUpdateVariableWidget(widgetId, globalVariableId, fontSize, title, background, shortcutId)
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
            val pendingIntent = if (shortcutId != null) {
                ExecuteActivity.IntentBuilder(shortcutId)
                    .trigger(ShortcutTriggerType.WIDGET)
                    .build(context)
                    .let { intent ->
                        PendingIntent.getActivity(
                            context,
                            variableWidget.widgetId,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
                        )
                    }
            } else {
                // There's no way to remove a click action, so instead we just set a dummy intent that won't be handled by anything
                val intent = Intent("http-shortcuts-dummy-action")
                PendingIntent.getBroadcast(context, variableWidget.widgetId, intent, PendingIntent.FLAG_IMMUTABLE)
            }

            views.setOnClickPendingIntent(R.id.widget_base, pendingIntent)

            val background = variableWidget.background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && background is WidgetBackgroundType.Color) {
                views.setColorIntSafely(
                    clazz = FrameLayout::class.java,
                    id = R.id.widget_container,
                    methodName = "setBackgroundColor",
                    value = background.color,
                )
            } else {
                views.setIntSafely(
                    clazz = FrameLayout::class.java,
                    id = R.id.widget_container,
                    methodName = "setBackgroundResource",
                    value = R.drawable.variable_widget_background,
                )
            }

            val text = globalVariable?.value?.ifEmpty { "-" } ?: "???"
            val fontSize = variableWidget.fontSize.toFloat()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val items = RemoteViews.RemoteCollectionItems.Builder()
                    .setHasStableIds(true)
                    .setViewTypeCount(1)
                    .addItem(
                        0,
                        RemoteViews(context.packageName, R.layout.variable_widget_content).also { innerViews ->
                            configureTextView(innerViews, fontSize, text)
                            innerViews.setOnClickFillInIntent(R.id.widget_content_container, Intent())
                        },
                    )
                    .build()
                views.setRemoteAdapter(R.id.widget_list, items)
                views.setPendingIntentTemplate(R.id.widget_list, pendingIntent)
            } else {
                configureTextView(views, fontSize, text)
            }

            if (variableWidget.title.isNotEmpty()) {
                views.setViewVisibility(R.id.widget_title, View.VISIBLE)
                views.setTextViewText(R.id.widget_title, variableWidget.title)

                val titleFontSize = fontSize * 0.75f
                views.setTextSize(R.id.widget_title, titleFontSize)
                views.setLineHeight(R.id.widget_title, TypedValueCompat.dpToPx(titleFontSize, context.resources.displayMetrics).toInt())
            } else {
                views.setViewVisibility(R.id.widget_title, View.GONE)
            }

            AppWidgetManager.getInstance(context)
                .updateAppWidget(variableWidget.widgetId, views)
        }
    }

    private fun configureTextView(remoteViews: RemoteViews, fontSize: Float, text: String) {
        remoteViews.setTextViewText(R.id.widget_text, text)
        remoteViews.setTextSize(R.id.widget_text, fontSize)
        remoteViews.setLineHeight(R.id.widget_text, TypedValueCompat.dpToPx(fontSize, context.resources.displayMetrics).toInt())
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        variableWidgetsRepository.deleteDeadVariableWidgets()
        variableWidgetsRepository.deleteVariableWidgets(widgetIds)
    }
}
