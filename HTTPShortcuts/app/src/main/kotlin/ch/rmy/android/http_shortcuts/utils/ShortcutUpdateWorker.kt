package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.widget.ShortcutWidgetManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@HiltWorker
class ShortcutUpdateWorker
@AssistedInject
constructor(
    @Assisted
    private val context: Context,
    @Assisted params: WorkerParameters,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
    private val shortcutWidgetManager: ShortcutWidgetManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val shortcutId = inputData.getString(DATA_SHORTCUT_ID) ?: return Result.failure()
        tryOrLog {
            launcherShortcutUpdater.updatePinnedShortcut(shortcutId)
        }
        tryOrLog {
            shortcutWidgetManager.updateWidgets(shortcutId)
        }
        return Result.success()
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke(shortcutId: ShortcutId) {
            with(WorkManager.getInstance(context)) {
                enqueue(
                    OneTimeWorkRequestBuilder<ShortcutUpdateWorker>()
                        .setInputData(
                            Data.Builder()
                                .putString(DATA_SHORTCUT_ID, shortcutId)
                                .build(),
                        )
                        .setInitialDelay(5.seconds.toJavaDuration())
                        .build(),
                )
            }
        }
    }

    companion object {
        private const val DATA_SHORTCUT_ID = "shortcutId"
    }
}
