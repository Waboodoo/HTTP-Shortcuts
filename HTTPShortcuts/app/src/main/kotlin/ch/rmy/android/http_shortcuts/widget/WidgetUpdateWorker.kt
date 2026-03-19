package ch.rmy.android.http_shortcuts.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class WidgetUpdateWorker
@AssistedInject
constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val shortcutWidgetManager: ShortcutWidgetManager,
    private val variableWidgetManager: VariableWidgetManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val type = Type.deserialize(inputData.getInt(KEY_TYPE, -1)) ?: return Result.failure()
        val widgetIds = inputData.getIntArray(KEY_WIDGET_IDS)?.toList() ?: return Result.failure()
        val action = Action.deserialize(inputData.getInt(KEY_ACTION, -1)) ?: return Result.failure()

        when (type) {
            Type.SHORTCUT -> {
                when (action) {
                    Action.UPDATE -> shortcutWidgetManager.updateWidgets(widgetIds)
                    Action.DELETE -> shortcutWidgetManager.deleteWidgets(widgetIds)
                }
            }
            Type.VARIABLE -> {
                when (action) {
                    Action.UPDATE -> variableWidgetManager.updateWidgets(widgetIds)
                    Action.DELETE -> variableWidgetManager.deleteWidgets(widgetIds)
                }
            }
        }
        return Result.success()
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        fun updateShortcutWidgets(widgetIds: IntArray) {
            enqueue(type = Type.SHORTCUT, widgetIds, action = Action.UPDATE)
        }

        fun deleteShortcutWidgets(widgetIds: IntArray) {
            enqueue(type = Type.SHORTCUT, widgetIds, action = Action.DELETE)
        }

        fun updateVariableWidgets(widgetIds: IntArray) {
            enqueue(type = Type.VARIABLE, widgetIds, action = Action.UPDATE)
        }

        fun deleteVariableWidgets(widgetIds: IntArray) {
            enqueue(type = Type.VARIABLE, widgetIds, action = Action.DELETE)
        }

        private fun enqueue(type: Type, widgetIds: IntArray, action: Action) {
            with(WorkManager.getInstance(context)) {
                enqueue(
                    OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .setInputData(
                            Data.Builder()
                                .putInt(KEY_TYPE, type.serialized)
                                .putIntArray(KEY_WIDGET_IDS, widgetIds)
                                .putInt(KEY_ACTION, action.serialized)
                                .build(),
                        )
                        .build(),
                )
            }
        }
    }

    private enum class Type(val serialized: Int) {
        SHORTCUT(0),
        VARIABLE(1),
        ;

        companion object {
            fun deserialize(value: Int) =
                entries.find { it.serialized == value }
        }
    }

    private enum class Action(val serialized: Int) {
        UPDATE(0),
        DELETE(1),
        ;

        companion object {
            fun deserialize(value: Int) =
                entries.find { it.serialized == value }
        }
    }

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_WIDGET_IDS = "widget_ids"
        private const val KEY_ACTION = "action"
    }
}
