package ch.rmy.android.http_shortcuts.sync

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.userError
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.http.buildRequest
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.ImportMode
import ch.rmy.android.http_shortcuts.import_export.ImportPasswordException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import ch.rmy.android.http_shortcuts.utils.WorkingDirectoryUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
    @Assisted
    private val context: Context,
    @Assisted params: WorkerParameters,
    private val userPreferences: UserPreferences,
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val syncRepository: SyncRepository,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
    private val importer: Importer,
    private val exporter: Exporter,
    private val historyEventLogger: HistoryEventLogger,
    private val httpClientFactory: HttpClientFactory,
    private val workingDirectoryUtil: WorkingDirectoryUtil,
    private val syncConfigMonitor: SyncConfigMonitor,
) : CoroutineWorker(context, params) {
    private val isSingleRun
        get() = SINGLE_TAG in tags

    override suspend fun doWork(): Result {
        if (isSingleRun) {
            // Artificially slow down the sync, so that it's easier to see in the UI that something happened
            delay(Random.nextInt(400, 900).milliseconds)
        } else if (syncConfigMonitor.isConfigurationInProgress()) {
            return Result.retry()
        }

        val syncType = userPreferences.syncType ?: return Result.success()
        val config = syncRepository.getConfig(syncType)

        try {
            when (config.type) {
                SyncType.IMPORT -> runImport(config)
                SyncType.EXPORT -> runExport(config)
            }
            deviceLocalPreferences.syncErrorCount = 0
            deviceLocalPreferences.syncTooManyErrors = false
            syncRepository.setLastSucceeded(syncType, Instant.now())
            return Result.success()
        } catch (e: Exception) {
            syncRepository.setLastFailed(syncType, Instant.now())
            if (e !is IOException && e !is ImportException && e !is UserException) {
                logException(e)
            }
            return if (isSingleRun) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    private suspend fun runImport(config: SyncConfig) {
        try {
            val importStatus = when (config.targetType) {
                SyncTargetType.FILE -> importFromFile(config)
                SyncTargetType.URL -> importFromWeb(config)
            }
            historyEventLogger.logEvent(
                HistoryEvent.SyncImportSucceed(
                    importedShortcuts = importStatus.importedShortcuts,
                ),
            )
        } catch (e: Exception) {
            incrementAndCheckErrorCount(e)
            historyEventLogger.logEvent(
                HistoryEvent.SyncImportFailed(
                    details = getErrorMessage(e),
                ),
            )
            throw e
        }
    }

    private suspend fun importFromFile(config: SyncConfig): Importer.ImportStatus {
        val directory = config.targetDirectoryUri
            ?.let { directoryUri ->
                workingDirectoryUtil.getDocumentFile(directoryUri)
            }
            ?: userError("No directory set")

        val fileName = config.targetFileName?.replacePlaceholders()
        val file = if (fileName != null) {
            directory.findFile(fileName)
                ?: userError("No file named '$fileName' found in directory")
        } else {
            directory.listFiles()
                .filter { file -> file.isFile && file.name?.endsWith(".zip", ignoreCase = true) == true }
                .maxByOrNull { file -> file.lastModified() }
                ?: userError("No ZIP file found in directory")
        }
        return import(config, targetUri = file.uri)
    }

    private fun SyncConfig.getImportMode() =
        if (replaceLocal) ImportMode.REPLACE else ImportMode.MERGE

    private suspend fun importFromWeb(config: SyncConfig): Importer.ImportStatus {
        val tempFile = File(context.cacheDir, "sync-import.zip")
        try {
            tempFile.delete()
            val client = httpClientFactory.getClient(context)
            val url = config.targetUrl
                ?.replacePlaceholders()
                ?: userError("No URL set")
            val call = client.newCall(
                buildRequest("GET", url) {
                    userAgent(UserAgentProvider.getUserAgent(context))
                    if (config.targetAuthUsername != null && config.targetAuthPassword != null) {
                        basicAuth(config.targetAuthUsername, config.targetAuthPassword)
                    }
                },
            )
            withContext(Dispatchers.IO) {
                call.execute()
            }
                .use { response ->
                    if (!response.isSuccessful) {
                        userError {
                            getString(R.string.error_sync_server_error, "${response.code} ${response.message}")
                        }
                    }
                    tempFile.outputStream().use { outputStream ->
                        response.body.byteStream().copyTo(outputStream)
                    }
                }

            return import(config, targetUri = FileUtil.getUriFromFile(context, tempFile))
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun import(config: SyncConfig, targetUri: Uri): Importer.ImportStatus =
        importer.importFromUri(
            uri = targetUri,
            importMode = config.getImportMode(),
            password = config.filePassword.takeUnlessEmpty(),
        )

    private suspend fun runExport(config: SyncConfig) {
        try {
            val exportStatus = when (config.targetType) {
                SyncTargetType.FILE -> exportToFile(config)
                SyncTargetType.URL -> exportToWeb(config)
            }
            historyEventLogger.logEvent(
                HistoryEvent.SyncExportSucceed(
                    exportedShortcuts = exportStatus.exportedShortcuts,
                ),
            )
        } catch (e: Exception) {
            incrementAndCheckErrorCount(e)
            historyEventLogger.logEvent(
                HistoryEvent.SyncExportFailed(details = getErrorMessage(e)),
            )
            throw e
        }
    }

    private suspend fun exportToFile(config: SyncConfig): Exporter.ExportStatus {
        val fileName = (config.targetFileName ?: SyncConfig.DEFAULT_FILE_NAME)
            .replacePlaceholders()

        val directory = config.targetDirectoryUri
            ?.let { directoryUri ->
                workingDirectoryUtil.getDocumentFile(directoryUri)
            }
            ?: userError("No directory set")
        directory.findFile(fileName)?.delete()
        val documentFile = directory.createFile(MIME_TYPE, fileName)
            ?: userError("Failed to create file, directory might not exist or no permission granted")
        return export(config, targetUri = documentFile.uri)
    }

    private suspend fun exportToWeb(config: SyncConfig): Exporter.ExportStatus {
        val tempFile = File(context.cacheDir, "sync-export.zip")
        try {
            tempFile.delete()
            val result = export(config, targetUri = FileUtil.getUriFromFile(context, tempFile))
            val client = httpClientFactory.getClient(context)
            val url = config.targetUrl
                ?.replacePlaceholders()
                ?: userError("No URL set")
            val call = client.newCall(
                buildRequest("PUT", url) {
                    userAgent(UserAgentProvider.getUserAgent(context))
                    if (config.targetAuthUsername != null && config.targetAuthPassword != null) {
                        basicAuth(config.targetAuthUsername, config.targetAuthPassword)
                    }
                    contentType(MIME_TYPE)
                    body(tempFile.inputStream(), tempFile.length())
                },
            )
            withContext(Dispatchers.IO) {
                call.execute()
            }
                .use { response ->
                    if (!response.isSuccessful) {
                        userError {
                            getString(R.string.error_sync_server_error, "${response.code} ${response.message}")
                        }
                    }
                }
            return result
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun export(config: SyncConfig, targetUri: Uri): Exporter.ExportStatus {
        val shortcutIds = config.categoryIds
            ?.let { categoryIds ->
                val allCategoryIds = categoryRepository.getCategoryIds().toSet()
                categoryIds.filter { it in allCategoryIds }
            }
            ?.let { categoryIds ->
                shortcutRepository.getShortcuts()
                    .mapNotNull { shortcut ->
                        if (shortcut.categoryId in categoryIds) shortcut.id else null
                    }
            }
        return exporter.exportToUri(
            uri = targetUri,
            shortcutIds = shortcutIds,
            password = config.filePassword.takeUnlessEmpty(),
            excludeDefaults = true,
        )
    }

    private fun String.replacePlaceholders(): String {
        val now = Instant.now().atZone(ZoneId.systemDefault())
        return this.replace("%D", DateTimeFormatter.ofPattern("dd").format(now))
            .replace("%M", DateTimeFormatter.ofPattern("MM").format(now))
            .replace("%Y", DateTimeFormatter.ofPattern("yyyy").format(now))
    }

    private fun incrementAndCheckErrorCount(e: Exception) {
        if (isSingleRun || e is IOException) {
            return
        }
        if (deviceLocalPreferences.syncErrorCount++ > 5) {
            userPreferences.syncType = null
            deviceLocalPreferences.syncTooManyErrors = true
            deviceLocalPreferences.syncErrorCount = 0
        }
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        fun scheduleRepeating(interval: Duration, requiresNetwork: Boolean) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(SCHEDULE_TAG)
                enqueue(
                    PeriodicWorkRequestBuilder<SyncWorker>(interval.toJavaDuration())
                        .addTag(TAG)
                        .addTag(SCHEDULE_TAG)
                        .setInitialDelay(5.minutes.toJavaDuration())
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5.minutes.toJavaDuration())
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .runIf(requiresNetwork) {
                                    setRequiredNetworkType(NetworkType.CONNECTED)
                                }
                                .build(),
                        )
                        .build(),
                )
            }
        }

        suspend fun scheduleNow() {
            with(WorkManager.getInstance(context)) {
                pruneWork().await()
                cancelAllWorkByTag(SINGLE_TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<SyncWorker>()
                        .addTag(TAG)
                        .addTag(SINGLE_TAG)
                        .build(),
                )
            }
        }

        fun cancel() {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        fun observeState(): Flow<WorkInfo.State?> =
            WorkManager.getInstance(context)
                .getWorkInfosByTagFlow(TAG)
                .map { workInfos ->
                    var state: WorkInfo.State? = null
                    workInfos.forEach { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.RUNNING -> state = WorkInfo.State.RUNNING
                            WorkInfo.State.SUCCEEDED -> {
                                if (state != WorkInfo.State.RUNNING) {
                                    state = WorkInfo.State.SUCCEEDED
                                }
                            }
                            WorkInfo.State.FAILED -> {
                                if (state != WorkInfo.State.RUNNING) {
                                    state = WorkInfo.State.FAILED
                                }
                            }
                            else -> Unit
                        }
                    }
                    state
                }
                .distinctUntilChanged()
    }

    private fun getErrorMessage(e: Exception): String? =
        when (e) {
            is ImportPasswordException -> context.getString(R.string.error_sync_import_wrong_password)
            is UserException -> e.getLocalizedMessage(context)
            else -> e.message
        }

    companion object {
        private const val TAG = "sync_worker"
        private const val SCHEDULE_TAG = "scheduled_sync_worker"
        private const val SINGLE_TAG = "single_sync_worker"

        private const val MIME_TYPE = "application/zip"
    }
}
