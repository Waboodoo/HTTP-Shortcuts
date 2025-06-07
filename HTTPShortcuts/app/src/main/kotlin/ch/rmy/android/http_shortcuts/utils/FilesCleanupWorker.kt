package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class FilesCleanupWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val shortcutRepository: ShortcutRepository,
    private val categoryRepository: CategoryRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        try {
            withContext(Dispatchers.IO) {
                FileUtil.deleteOldCacheFiles(context, maxCacheFileAge = 5.minutes)
                deleteOldRasterizedBuiltInIcons(context)
            }
            Result.success()
        } catch (e: Exception) {
            logException(e)
            Result.failure()
        }

    private suspend fun deleteOldRasterizedBuiltInIcons(context: Context) {
        val iconFiles = context.filesDir
            .listFiles { IconUtil.isRasterizedIconFileName(it.name) }
            ?.takeUnless { it.isEmpty() }
            ?: return

        val iconsInUse = shortcutRepository.getShortcuts().map(Shortcut::icon)
            .plus(categoryRepository.getCategories().mapNotNull(Category::icon))
            .filterIsInstance<ShortcutIcon.BuiltInIcon>()
            .flatMap { icon ->
                listOf(
                    IconUtil.getRasterizedIconFileName(icon, adaptive = true),
                    IconUtil.getRasterizedIconFileName(icon, adaptive = false),
                )
            }
            .toSet()

        iconFiles.forEach { iconFile ->
            if (iconFile.name !in iconsInUse) {
                iconFile.delete()
            }
        }
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke() {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<FilesCleanupWorker>()
                        .addTag(TAG)
                        .setInitialDelay(CLEANUP_DELAY.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .build(),
                        )
                        .build(),
                )
            }
        }
    }

    companion object {
        private const val TAG = "files-cleanup"
        private val CLEANUP_DELAY = 10.seconds
    }
}
