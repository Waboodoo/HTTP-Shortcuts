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
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.widget.ShortcutWidgetManager
import ch.rmy.android.http_shortcuts.widget.VariableWidgetManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class ShortcutUpdateWorker
@AssistedInject
constructor(
    @Assisted
    private val context: Context,
    @Assisted params: WorkerParameters,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
    private val shortcutWidgetManager: ShortcutWidgetManager,
    private val variableWidgetManager: VariableWidgetManager,
    private val shortcutRepository: ShortcutRepository,
    private val secondaryLauncherManager: SecondaryLauncherManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val shortcutId = inputData.getString(DATA_SHORTCUT_ID)
        if (shortcutId != null) {
            tryOrLog {
                launcherShortcutUpdater.updatePinnedShortcut(shortcutId)
            }
            tryOrLog {
                shortcutWidgetManager.updateWidgets(shortcutId)
            }
        } else {
            tryOrLog {
                launcherShortcutUpdater.updateAllPinnedShortcuts()
            }
            tryOrLog {
                launcherShortcutUpdater.updateAllAppShortcuts()
            }
            tryOrLog {
                shortcutWidgetManager.updateAllWidgets()
            }
            tryOrLog {
                variableWidgetManager.updateAllWidgets()
            }
        }
        secondaryLauncherManager.setSecondaryLauncherVisibility(shortcutRepository.hasSecondaryLauncherShortcuts())
        return Result.success()
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke(shortcutId: ShortcutId? = null) {
            tryOrLog {
                with(WorkManager.getInstance(context)) {
                    enqueue(
                        OneTimeWorkRequestBuilder<ShortcutUpdateWorker>()
                            .setInputData(
                                Data.Builder()
                                    .putString(DATA_SHORTCUT_ID, shortcutId)
                                    .build(),
                            )
                            .build(),
                    )
                }
            }
        }
    }

    companion object {
        private const val DATA_SHORTCUT_ID = "shortcutId"
    }
}
