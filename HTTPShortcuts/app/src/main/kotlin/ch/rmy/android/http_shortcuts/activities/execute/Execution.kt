package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.activities.execute.types.ExecutionTypeFactory
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckWifiSSIDUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ExtractFileIdsFromVariableValuesUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.RequestBiometricConfirmationUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.RequestSimpleConfirmationUseCase
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.getRequestHeadersForShortcut
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcut
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.extensions.isTemporaryShortcut
import ch.rmy.android.http_shortcuts.extensions.resolve
import ch.rmy.android.http_shortcuts.extensions.shouldIncludeInHistory
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.plugin.SessionMonitor
import ch.rmy.android.http_shortcuts.scheduling.ExecutionSchedulerWorker
import ch.rmy.android.http_shortcuts.scripting.ResultHandler
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.CacheFilesCleanupWorker
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class Execution(
    private val context: Context,
    private val params: ExecutionParams,
    private val dialogHandle: DialogHandle,
) {
    private val entryPoint = EntryPointAccessors.fromApplication<ExecutionEntryPoint>(context)

    // Objects which are accessed multiple times are created immediately and a reference is kept
    private val scriptExecutor: ScriptExecutor = entryPoint.scriptExecutor()
    private val externalRequests: ExternalRequests = entryPoint.externalRequests()
    private val historyEventLogger: HistoryEventLogger = entryPoint.historyEventLogger()
    private val sessionMonitor: SessionMonitor = entryPoint.sessionMonitor()
    private val pendingExecutionsRepository: PendingExecutionsRepository = entryPoint.pendingExecutionsRepository()

    // Objects which are only accessed once or not at all are created lazily and no reference is kept
    private val shortcutRepository: ShortcutRepository
        get() = entryPoint.shortcutRepository()
    private val categoryRepository: CategoryRepository
        get() = entryPoint.categoryRepository()
    private val requestHeaderRepository: RequestHeaderRepository
        get() = entryPoint.requestHeaderRepository()
    private val requestParameterRepository: RequestParameterRepository
        get() = entryPoint.requestParameterRepository()
    private val appConfigRepository: AppConfigRepository
        get() = entryPoint.appConfigRepository()
    private val variableRepository: VariableRepository
        get() = entryPoint.variableRepository()
    private val variableResolver: VariableResolver
        get() = entryPoint.variableResolver()
    private val launcherShortcutManager: LauncherShortcutManager
        get() = entryPoint.launcherShortcutManager()
    private val requestSimpleConfirmation: RequestSimpleConfirmationUseCase
        get() = entryPoint.requestSimpleConfirmation()
    private val requestBiometricConfirmation: RequestBiometricConfirmationUseCase
        get() = entryPoint.requestBiometricConfirmation()
    private val checkWifiSSID: CheckWifiSSIDUseCase
        get() = entryPoint.checkWifiSSID()
    private val extractFileIdsFromVariableValues: ExtractFileIdsFromVariableValuesUseCase
        get() = entryPoint.extractFileIdsFromVariableValues()
    private val errorFormatter: ErrorFormatter
        get() = entryPoint.errorFormatter()
    private val cacheFilesCleanupStarter: CacheFilesCleanupWorker.Starter
        get() = entryPoint.cacheFilesCleanupStarter()
    private val historyCleanUpStarter: HistoryCleanUpWorker.Starter
        get() = entryPoint.historyCleanUpStarter()
    private val executionSchedulerStarter: ExecutionSchedulerWorker.Starter
        get() = entryPoint.executionSchedulerStarter()
    private val executionTypeFactory: ExecutionTypeFactory
        get() = entryPoint.executionTypeFactory()

    private lateinit var globalCode: String
    private lateinit var category: Category
    private lateinit var shortcut: Shortcut
    private var requestHeaders: List<RequestHeader> = emptyList()
    private var requestParameters: List<RequestParameter> = emptyList()

    private val shortcutName by lazy {
        shortcut.getSafeName(context)
    }

    fun execute(): Flow<ExecutionStatus> = flow {
        logInfo("Beginning to execute shortcut (${params.shortcutId}, trigger=${params.trigger ?: "unknown"})")
        sessionMonitor.onSessionStarted()
        emit(ExecutionStatus.Preparing)
        try {
            executeWithoutExceptionHandling()
        } catch (e: UserException) {
            displayError(e)
        } catch (e: NoActivityAvailableException) {
            throw CancellationException("Host activity disappeared, cancelling", e)
        } catch (e: CancellationException) {
            if (::shortcut.isInitialized && shortcut.shouldIncludeInHistory()) {
                historyEventLogger.logEvent(
                    HistoryEvent.ShortcutCancelled(
                        shortcutName = shortcut.name,
                    ),
                )
            }
            throw e
        } catch (e: Exception) {
            logError("Unknown / unexpected error, please contact developer")
            withContext(Dispatchers.Main) {
                context.showToast(R.string.error_generic)
            }
            logException(e)
        } finally {
            scriptExecutor.destroy()
            tryOrLog {
                cacheFilesCleanupStarter()
                historyCleanUpStarter()
                executionSchedulerStarter()
            }
        }
    }
        .flowOn(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        .onEach { status ->
            if (status is ExecutionStatus.WithResult) {
                sessionMonitor.onResult(status.result)
            }
        }
        .onCompletion {
            sessionMonitor.onSessionComplete()
        }

    private suspend fun displayError(error: Throwable) {
        val message = errorFormatter.getPrettyError(error, shortcutName, includeBody = true)
        if (shortcut.shouldIncludeInHistory()) {
            logError(message)
        }

        withContext(Dispatchers.Main) {
            try {
                dialogHandle.showDialog(
                    ExecuteDialogState.GenericMessage(
                        title = StringResLocalizable(R.string.dialog_title_error),
                        message = message.toLocalizable(),
                    ),
                )
            } catch (_: NoActivityAvailableException) {
                context.showToast(message, long = true)
            }
        }
    }

    private suspend fun FlowCollector<ExecutionStatus>.executeWithoutExceptionHandling() {
        if (params.executionId != null) {
            pendingExecutionsRepository.removePendingExecution(params.executionId)
        }

        try {
            loadData()
        } catch (_: NoSuchElementException) {
            dialogHandle.showDialog(
                ExecuteDialogState.GenericMessage(
                    title = StringResLocalizable(R.string.dialog_title_error),
                    message = StringResLocalizable(R.string.shortcut_not_found),
                ),
            )
            throw CancellationException("Cancelling because shortcut was not found")
        }

        scheduleRepetitionIfNeeded()

        if (shortcut.shouldIncludeInHistory()) {
            historyEventLogger.logEvent(
                HistoryEvent.ShortcutTriggered(
                    shortcutName = shortcut.name,
                    trigger = params.trigger,
                ),
            )
        }

        launcherShortcutManager.reportUse(params.shortcutId)

        when (requiresConfirmation()) {
            ConfirmationType.SIMPLE -> requestSimpleConfirmation(shortcutName, dialogHandle)
            ConfirmationType.BIOMETRIC -> requestBiometricConfirmation(shortcutName)
            null -> Unit
        }
        shortcut.wifiSsid?.let { wifiSsid ->
            checkWifiSSID(shortcutName, wifiSsid, dialogHandle)
        }

        val variableManager = VariableManager(
            variables = variableRepository.getVariables(),
            preResolvedValues = params.variableValues,
        )

        if (shouldDelayExecution()) {
            logInfo("Delaying execution")
            pendingExecutionsRepository.createPendingExecution(
                shortcutId = shortcut.id,
                resolvedVariables = variableManager.getVariableValuesByKeys(),
                delay = shortcut.delay.milliseconds,
                tryNumber = 1,
                recursionDepth = params.recursionDepth,
                requiresNetwork = shortcut.isWaitForNetwork,
                type = PendingExecutionType.INITIAL_DELAY,
            )
            return
        }

        val usesScripting = usesScripting()

        val fileUploadResult = handleFiles(loadMetaData = usesScripting)

        emit(ExecutionStatus.InProgress(variableManager.getVariableValuesByIds()))

        val resultHandler = ResultHandler()

        if (usesScripting) {
            logInfo("Initializing ScriptExecutor")
            scriptExecutor.initialize(
                shortcut = shortcut,
                category = category,
                variableManager = variableManager,
                fileUploadResult = fileUploadResult,
                resultHandler = resultHandler,
                dialogHandle = dialogHandle,
                recursionDepth = params.recursionDepth,
            )
        }

        if ((params.tryNumber == 0 || (params.tryNumber == 1 && shortcut.delay > 0)) && usesScripting) {
            scriptExecutor.execute(globalCode)
            scriptExecutor.execute(shortcut.codeOnPrepare)
        }

        logInfo("Resolving variables")
        variableResolver.resolve(
            variableManager = variableManager,
            shortcut = shortcut,
            headers = requestHeaders,
            parameters = requestParameters,
            dialogHandle = dialogHandle,
        )

        executionTypeFactory.createExecutionType(shortcut.executionType)
            .invoke(
                shortcut = shortcut,
                requestHeaders = requestHeaders,
                requestParameters = requestParameters,
                variableManager = variableManager,
                resultHandler = resultHandler,
                params = params,
                fileUploadResult = fileUploadResult,
                dialogHandle = dialogHandle,
                scriptExecutor = scriptExecutor,
            )
            .collect(this)
    }

    private fun requiresConfirmation() =
        shortcut.confirmationType?.takeIf { params.tryNumber == 0 }

    private fun shouldDelayExecution() =
        shortcut.delay > 0 && params.tryNumber == 0

    private suspend fun loadData() {
        globalCode = appConfigRepository.getGlobalCode()
        shortcut = shortcutRepository.getShortcutById(params.shortcutId)
        category = categoryRepository.getCategoryById(shortcut.categoryId)
        requestHeaders = requestHeaderRepository.getRequestHeadersForShortcut(shortcut)
        requestParameters = requestParameterRepository.getRequestParametersForShortcut(shortcut)
    }

    private suspend fun scheduleRepetitionIfNeeded() {
        if (shortcut.isTemporaryShortcut) {
            return
        }
        val repetitionInterval = shortcut.repetitionInterval ?: return
        pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcut.id)
        pendingExecutionsRepository
            .createPendingExecution(
                shortcutId = shortcut.id,
                delay = repetitionInterval.minutes,
                requiresNetwork = false,
                type = PendingExecutionType.REPEAT,
            )
    }

    private suspend fun handleFiles(loadMetaData: Boolean): FileUploadManager.Result? = coroutineScope {
        if (!shortcut.usesRequestParameters() && !shortcut.usesGenericFileBody()) {
            return@coroutineScope null
        }

        val fileUploadManager = FileUploadManager.Builder(context.contentResolver)
            .runIf(shortcut.usesGenericFileBody()) {
                addFileRequest(
                    multiple = shortcut.fileUploadType == FileUploadType.FILE_PICKER_MULTI,
                    withImageEditor = shortcut.fileUploadUseImageEditor,
                    fromFile = if (shortcut.fileUploadType == FileUploadType.FILE) {
                        shortcut.fileUploadSourceFile?.toUri()
                    } else {
                        null
                    },
                    fromCamera = shortcut.fileUploadType == FileUploadType.CAMERA,
                )
            }
            .runFor(requestParameters) { parameter ->
                when (parameter.parameterType) {
                    ParameterType.STRING -> this
                    ParameterType.FILE -> addFileRequest(
                        multiple = parameter.fileUploadType == FileUploadType.FILE_PICKER_MULTI,
                        withImageEditor = parameter.fileUploadUseImageEditor,
                        fromFile = if (parameter.fileUploadType == FileUploadType.FILE) {
                            parameter.fileUploadSourceFile?.toUri()
                        } else {
                            null
                        },
                        fromCamera = parameter.fileUploadType == FileUploadType.CAMERA,
                    )
                }
            }
            .withMetaData(loadMetaData)
            .withTransformation(::processFileIfNeeded)
            .build()
            .apply {
                registerSharedFiles(params.fileUris)
                registerForwardedFiles(extractFileIdsFromVariableValues(params.variableValues))
            }

        var fileRequest: FileUploadManager.FileRequest
        while (true) {
            fileRequest = fileUploadManager.getNextFileRequest() ?: break
            ensureActive()
            val files = when {
                fileRequest.fromFile != null -> {
                    listOf(fileRequest.fromFile)
                }
                fileRequest.fromCamera -> {
                    externalRequests.openCamera()
                }
                else -> {
                    externalRequests.openFilePicker(fileRequest.multiple)
                }
            }
            ensureActive()
            fileUploadManager.fulfillFileRequest(fileRequest, files)
        }

        fileUploadManager.getResult()
    }

    private suspend fun processFileIfNeeded(fileRequest: FileUploadManager.FileRequest, uri: Uri, mimeType: String): Uri? {
        if (fileRequest.withImageEditor && FileTypeUtil.isImage(mimeType)) {
            return externalRequests.cropImage(
                uri,
                compressFormat = when (mimeType) {
                    "image/png" -> Bitmap.CompressFormat.PNG
                    "image/jpg", "image/jpeg" -> Bitmap.CompressFormat.JPEG
                    else -> return null
                },
            )
        }
        return null
    }

    private fun usesScripting() =
        shortcut.codeOnPrepare.isNotEmpty() ||
            shortcut.codeOnSuccess.isNotEmpty() ||
            shortcut.codeOnFailure.isNotEmpty() ||
            globalCode.isNotEmpty()

    private fun logError(message: String) {
        historyEventLogger.logEvent(
            HistoryEvent.Error(
                shortcutName = if (::shortcut.isInitialized) shortcut.name else "???",
                error = message,
            ),
        )
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ExecutionEntryPoint {
        fun shortcutRepository(): ShortcutRepository
        fun pendingExecutionsRepository(): PendingExecutionsRepository
        fun categoryRepository(): CategoryRepository
        fun requestHeaderRepository(): RequestHeaderRepository
        fun requestParameterRepository(): RequestParameterRepository
        fun appConfigRepository(): AppConfigRepository
        fun variableRepository(): VariableRepository
        fun variableResolver(): VariableResolver
        fun launcherShortcutManager(): LauncherShortcutManager
        fun requestSimpleConfirmation(): RequestSimpleConfirmationUseCase
        fun requestBiometricConfirmation(): RequestBiometricConfirmationUseCase
        fun checkWifiSSID(): CheckWifiSSIDUseCase
        fun extractFileIdsFromVariableValues(): ExtractFileIdsFromVariableValuesUseCase
        fun scriptExecutor(): ScriptExecutor
        fun externalRequests(): ExternalRequests
        fun errorFormatter(): ErrorFormatter
        fun historyEventLogger(): HistoryEventLogger
        fun cacheFilesCleanupStarter(): CacheFilesCleanupWorker.Starter
        fun historyCleanUpStarter(): HistoryCleanUpWorker.Starter
        fun executionSchedulerStarter(): ExecutionSchedulerWorker.Starter
        fun sessionMonitor(): SessionMonitor
        fun executionTypeFactory(): ExecutionTypeFactory
    }
}
