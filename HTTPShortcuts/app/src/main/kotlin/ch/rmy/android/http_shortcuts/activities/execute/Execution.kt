package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckHeadlessExecutionUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckWifiSSIDUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ExtractFileIdsFromVariableValuesUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.OpenInBrowserUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.RequestBiometricConfirmationUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.RequestSimpleConfirmationUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ShowResultDialogUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ValidateRequestDataUseCase
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.activities.response.models.ResponseData
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.FileUploadOptions
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import ch.rmy.android.http_shortcuts.exceptions.TreatAsFailureException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.extensions.isTemporaryShortcut
import ch.rmy.android.http_shortcuts.extensions.resolve
import ch.rmy.android.http_shortcuts.extensions.shouldIncludeInHistory
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.HttpRequesterWorker
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.plugin.SessionMonitor
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.scheduling.ExecutionSchedulerWorker
import ch.rmy.android.http_shortcuts.scripting.ResultHandler
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.CacheFilesCleanupWorker
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.ext.copyFromRealm
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Execution(
    private val context: Context,
    private val params: ExecutionParams,
    private val dialogHandle: DialogHandle,
) {
    private val entryPoint = EntryPointAccessors.fromApplication<ExecutionEntryPoint>(context)
    private val shortcutRepository: ShortcutRepository = entryPoint.shortcutRepository()
    private val pendingExecutionsRepository: PendingExecutionsRepository = entryPoint.pendingExecutionsRepository()
    private val variableRepository: VariableRepository = entryPoint.variableRepository()
    private val appRepository: AppRepository = entryPoint.appRepository()
    private val workingDirectoryRepository: WorkingDirectoryRepository = entryPoint.workingDirectoryRepository()
    private val variableResolver: VariableResolver = entryPoint.variableResolver()
    private val httpRequester: HttpRequester = entryPoint.httpRequester()
    private val showResultDialog: ShowResultDialogUseCase = entryPoint.showResultDialog()
    private val executionScheduler: ExecutionScheduler = entryPoint.executionScheduler()
    private val launcherShortcutManager: LauncherShortcutManager = entryPoint.launcherShortcutManager()
    private val openInBrowser: OpenInBrowserUseCase = entryPoint.openInBrowser()
    private val requestSimpleConfirmation: RequestSimpleConfirmationUseCase = entryPoint.requestSimpleConfirmation()
    private val requestBiometricConfirmation: RequestBiometricConfirmationUseCase = entryPoint.requestBiometricConfirmation()
    private val checkWifiSSID: CheckWifiSSIDUseCase = entryPoint.checkWifiSSID()
    private val networkUtil: NetworkUtil = entryPoint.networkUtil()
    private val extractFileIdsFromVariableValues: ExtractFileIdsFromVariableValuesUseCase = entryPoint.extractFileIdsFromVariableValues()
    private val scriptExecutor: ScriptExecutor = entryPoint.scriptExecutor()
    private val externalRequests: ExternalRequests = entryPoint.externalRequests()
    private val httpRequesterStarter: HttpRequesterWorker.Starter = entryPoint.httpRequesterStarter()
    private val checkHeadlessExecution: CheckHeadlessExecutionUseCase = entryPoint.checkHeadlessExecution()
    private val errorFormatter: ErrorFormatter = entryPoint.errorFormatter()
    private val historyEventLogger: HistoryEventLogger = entryPoint.historyEventLogger()
    private val cacheFilesCleanupStarter: CacheFilesCleanupWorker.Starter = entryPoint.cacheFilesCleanupStarter()
    private val historyCleanUpStarter: HistoryCleanUpWorker.Starter = entryPoint.historyCleanUpStarter()
    private val executionSchedulerStarter: ExecutionSchedulerWorker.Starter = entryPoint.executionSchedulerStarter()
    private val sessionMonitor: SessionMonitor = entryPoint.sessionMonitor()
    private val navigationArgStore: NavigationArgStore = entryPoint.navigationArgStore()
    private val validateRequestData: ValidateRequestDataUseCase = entryPoint.validateRequestData()

    private lateinit var globalCode: String
    private lateinit var category: Category
    private lateinit var shortcut: Shortcut
    private var workingDirectory: WorkingDirectory? = null
    private lateinit var certificatePins: List<CertificatePin>

    private val shortcutName by lazy {
        shortcut.getSafeName(context)
    }

    suspend fun execute(): Flow<ExecutionStatus> = flow {
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
                    )
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
            tryOrLog {
                cacheFilesCleanupStarter()
                historyCleanUpStarter()
                executionSchedulerStarter()
            }
        }
    }
        .onEach { status ->
            if (status is ExecutionStatus.WithResult) {
                sessionMonitor.onResult(status.result)
            }
        }
        .onCompletion {
            sessionMonitor.onSessionComplete()
        }

    private suspend fun displayError(error: Throwable) {
        generateOutputFromError(error)
            .let { message ->
                if (shortcut.shouldIncludeInHistory()) {
                    logError(message)
                }

                withContext(Dispatchers.Main) {
                    try {
                        dialogHandle.showDialog(
                            ExecuteDialogState.GenericMessage(
                                title = StringResLocalizable(R.string.dialog_title_error),
                                message = message.toLocalizable(),
                            )
                        )
                    } catch (e: NoActivityAvailableException) {
                        context.showToast(message, long = true)
                    }
                }
            }
    }

    private fun generateOutputFromError(error: Throwable, simple: Boolean = false) =
        errorFormatter.getPrettyError(error, shortcutName, includeBody = !simple)

    private suspend fun FlowCollector<ExecutionStatus>.executeWithoutExceptionHandling() {
        if (params.executionId != null) {
            pendingExecutionsRepository.removePendingExecution(params.executionId)
        }

        try {
            loadData()
        } catch (e: NoSuchElementException) {
            dialogHandle.showDialog(
                ExecuteDialogState.GenericMessage(
                    title = StringResLocalizable(R.string.dialog_title_error),
                    message = StringResLocalizable(R.string.shortcut_not_found),
                )
            )
            throw CancellationException("Cancelling because shortcut was not found")
        }

        scheduleRepetitionIfNeeded()

        if (shortcut.shouldIncludeInHistory()) {
            historyEventLogger.logEvent(
                HistoryEvent.ShortcutTriggered(
                    shortcutName = shortcut.name,
                    trigger = params.trigger,
                )
            )
        }

        launcherShortcutManager.reportUse(params.shortcutId)

        when (requiresConfirmation()) {
            ConfirmationType.SIMPLE -> requestSimpleConfirmation(shortcutName, dialogHandle)
            ConfirmationType.BIOMETRIC -> requestBiometricConfirmation(shortcutName)
            null -> Unit
        }
        checkWifiSSID(shortcutName, shortcut.wifiSsid, dialogHandle)

        val variableManager = VariableManager(variableRepository.getVariables().map { it.copyFromRealm() }, params.variableValues)

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
        variableResolver.resolve(variableManager, shortcut, dialogHandle)

        when (shortcut.type) {
            ShortcutExecutionType.BROWSER -> {
                emit(
                    ExecutionStatus.WrappingUp(
                        variableManager.getVariableValuesByIds(),
                        result = resultHandler.getResult(),
                    )
                )
                openShortcutInBrowser(variableManager)
                return
            }
            ShortcutExecutionType.SCRIPTING,
            ShortcutExecutionType.TRIGGER,
            -> {
                emit(
                    ExecutionStatus.WrappingUp(
                        variableManager.getVariableValuesByIds(),
                        result = resultHandler.getResult(),
                    )
                )
                return
            }
            ShortcutExecutionType.APP -> {
                // continue
            }
        }

        val sessionId = "${shortcut.id}_${newUUID()}"

        if (params.recursionDepth == 0 && checkHeadlessExecution(shortcut, variableManager.getVariableValuesByIds())) {
            logInfo("Preparing to execute HTTP request in headless mode")
            try {
                httpRequesterStarter.invoke(
                    shortcutId = shortcut.id,
                    sessionId = sessionId,
                    variableValues = variableManager.getVariableValuesByIds(),
                    fileUploadResult = fileUploadResult,
                )
                return
            } catch (e: Throwable) {
                logException(e)
            }
        }

        val response = try {
            try {
                httpRequester
                    .executeShortcut(
                        context,
                        shortcut = shortcut,
                        storeDirectoryUri = workingDirectory?.directoryUri,
                        sessionId = sessionId,
                        variableValues = variableManager.getVariableValuesByIds(),
                        fileUploadResult = fileUploadResult,
                        useCookieJar = shortcut.acceptCookies,
                        certificatePins = certificatePins,
                        validateRequestData = { requestData ->
                            validateRequestData(dialogHandle, shortcut, requestData)
                        },
                    )
            } catch (e: UnknownHostException) {
                if (shouldReschedule(e)) {
                    if (shortcut.responseHandling?.successOutput != ResponseHandling.SUCCESS_OUTPUT_NONE && params.tryNumber == 0) {
                        withContext(Dispatchers.Main) {
                            context.showToast(
                                String.format(
                                    context.getString(R.string.execution_delayed),
                                    shortcutName,
                                ),
                                long = true,
                            )
                        }
                    }
                    rescheduleExecution(variableManager)
                    executionScheduler.schedule()
                    return
                }
                throw e
            }
        } catch (e: Exception) {
            if (e is ErrorResponse || e is IOException) {
                scriptExecutor.execute(
                    script = shortcut.codeOnFailure,
                    error = e,
                )

                when (val failureOutput = shortcut.responseHandling?.failureOutput) {
                    ResponseHandling.FAILURE_OUTPUT_DETAILED,
                    ResponseHandling.FAILURE_OUTPUT_SIMPLE,
                    -> {
                        displayResult(
                            generateOutputFromError(e, simple = failureOutput == ResponseHandling.FAILURE_OUTPUT_SIMPLE),
                            response = (e as? ErrorResponse)?.shortcutResponse,
                        )
                    }
                    else -> Unit
                }

                emit(
                    ExecutionStatus.CompletedWithError(
                        error = e as? IOException,
                        response = (e as? ErrorResponse)?.shortcutResponse,
                        variableValues = variableManager.getVariableValuesByIds(),
                        result = resultHandler.getResult(),
                    ),
                )
                return
            }
            throw e
        }

        try {
            scriptExecutor.registerAbortAndTreatAsFailure()
            scriptExecutor.execute(
                script = shortcut.codeOnSuccess,
                response = response,
            )
        } catch (e: TreatAsFailureException) {
            scriptExecutor.execute(
                script = shortcut.codeOnFailure,
                error = ErrorResponse(response),
            )

            when (val failureOutput = shortcut.responseHandling?.failureOutput) {
                ResponseHandling.FAILURE_OUTPUT_DETAILED,
                ResponseHandling.FAILURE_OUTPUT_SIMPLE,
                -> {
                    displayResult(
                        generateOutputFromError(e, simple = failureOutput == ResponseHandling.FAILURE_OUTPUT_SIMPLE),
                        response = response,
                    )
                }
                else -> Unit
            }

            emit(
                ExecutionStatus.CompletedWithError(
                    error = null,
                    response = response,
                    variableValues = variableManager.getVariableValuesByIds(),
                    result = resultHandler.getResult(),
                ),
            )
            return
        }

        if (shortcut.responseHandling?.storeDirectoryId != null && response.contentFile != null) {
            workingDirectoryRepository.touchWorkingDirectory(shortcut.responseHandling!!.storeDirectoryId!!)
            withContext(Dispatchers.IO) {
                workingDirectory?.directoryUri?.let {
                    renameResponseFile(response, variableManager, it)
                }
            }
        }

        emit(
            ExecutionStatus.WrappingUp(
                variableManager.getVariableValuesByIds(),
                result = resultHandler.getResult(),
            )
        )
        handleDisplayingOfResult(response, variableManager)
        logInfo("Execution completed successfully (${params.shortcutId})")
        emit(
            ExecutionStatus.CompletedSuccessfully(
                response = response,
                variableValues = variableManager.getVariableValuesByIds(),
                result = resultHandler.getResult(),
            )
        )
    }

    private fun shouldReschedule(error: Throwable): Boolean =
        shortcut.isWaitForNetwork &&
            error !is ErrorResponse &&
            !networkUtil.isNetworkConnected()

    private suspend fun rescheduleExecution(variableManager: VariableManager) {
        if (params.tryNumber < MAX_RETRY) {
            pendingExecutionsRepository
                .createPendingExecution(
                    shortcutId = shortcut.id,
                    resolvedVariables = variableManager.getVariableValuesByKeys(),
                    tryNumber = params.tryNumber + 1,
                    delay = calculateDelay(),
                    recursionDepth = params.recursionDepth,
                    requiresNetwork = shortcut.isWaitForNetwork,
                    type = PendingExecutionType.RETRY_LATER,
                )
        }
    }

    private fun calculateDelay() =
        (RETRY_BACKOFF.pow(params.tryNumber.toDouble()).toInt()).seconds

    private fun shouldDelayExecution() =
        shortcut.delay > 0 && params.tryNumber == 0

    private suspend fun loadData() {
        coroutineScope {
            val base = appRepository.getBase()
            globalCode = base.globalCode.orEmpty()
            if (!findCategoryAndShortcut(base)) {
                throw NoSuchElementException()
            }
            workingDirectory = shortcut.responseHandling?.storeDirectoryId?.let { workingDirectoryId ->
                base.workingDirectories.find { it.id == workingDirectoryId }
            }
            logInfo("Shortcut loaded: type=${shortcut.type}")
            certificatePins = base.certificatePins
        }
    }

    private suspend fun findCategoryAndShortcut(base: Base): Boolean {
        if (params.shortcutId == Shortcut.TEMPORARY_ID) {
            shortcut = shortcutRepository.getShortcutById(Shortcut.TEMPORARY_ID)
            category = if (shortcut.categoryId == null) {
                logException(IllegalStateException("categoryId was not set in temporary shortcut"))
                null
            } else {
                base.categories.firstOrNull { it.id == shortcut.categoryId!! }
                    .also {
                        if (it == null) {
                            logException(IllegalStateException("Temporary shortcut's category not found"))
                        }
                    }
            }
                ?: Category().apply { id = "unknown" }
            return true
        }
        for (category in base.categories) {
            for (shortcut in category.shortcuts) {
                if (shortcut.id == params.shortcutId) {
                    this@Execution.category = category
                    this@Execution.shortcut = shortcut
                    return true
                }
            }
        }
        return false
    }

    private suspend fun scheduleRepetitionIfNeeded() {
        if (shortcut.isTemporaryShortcut) {
            return
        }
        val repetition = shortcut.repetition ?: return
        pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcut.id)
        pendingExecutionsRepository
            .createPendingExecution(
                shortcutId = shortcut.id,
                delay = repetition.interval.minutes,
                requiresNetwork = false,
                type = PendingExecutionType.REPEAT,
            )
    }

    private fun requiresConfirmation() =
        shortcut.confirmationType?.takeIf { params.tryNumber == 0 }

    private suspend fun handleFiles(loadMetaData: Boolean): FileUploadManager.Result? = coroutineScope {
        if (!shortcut.usesRequestParameters() && !shortcut.usesGenericFileBody()) {
            return@coroutineScope null
        }

        val fileUploadManager = FileUploadManager.Builder(context.contentResolver)
            .runIf(shortcut.usesGenericFileBody()) {
                addFileRequest(shortcut.fileUploadOptions)
            }
            .runFor(shortcut.parameters) { parameter ->
                when (parameter.parameterType) {
                    ParameterType.STRING -> this
                    ParameterType.FILE -> addFileRequest(parameter.fileUploadOptions)
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
                    listOf(fileRequest.fromFile!!)
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

    private fun FileUploadManager.Builder.addFileRequest(fileUploadOptions: FileUploadOptions?): FileUploadManager.Builder =
        addFileRequest(
            multiple = fileUploadOptions?.type == FileUploadType.FILE_PICKER_MULTI,
            withImageEditor = fileUploadOptions?.useImageEditor == true,
            fromFile = fileUploadOptions?.takeIf { it.type == FileUploadType.FILE }?.file?.toUri(),
            fromCamera = fileUploadOptions?.type == FileUploadType.CAMERA,
        )

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

    private suspend fun openShortcutInBrowser(variableManager: VariableManager) {
        openInBrowser(url = injectVariables(shortcut.url, variableManager), targetBrowser = shortcut.targetBrowser)
    }

    private fun injectVariables(string: String, variableManager: VariableManager): String =
        Variables.rawPlaceholdersToResolvedValues(string, variableManager.getVariableValuesByIds())

    private suspend fun handleDisplayingOfResult(response: ShortcutResponse, variableManager: VariableManager) {
        when (shortcut.responseHandling?.successOutput) {
            ResponseHandling.SUCCESS_OUTPUT_MESSAGE -> {
                displayResult(
                    output = shortcut.responseHandling
                        ?.successMessage
                        ?.takeUnlessEmpty()
                        ?.let {
                            injectVariables(it, variableManager)
                        }
                        ?: context.getString(R.string.executed, shortcutName),
                    response = response,
                )
            }
            ResponseHandling.SUCCESS_OUTPUT_RESPONSE -> displayResult(output = null, response)
        }
    }

    private suspend fun displayResult(output: String?, response: ShortcutResponse? = null) {
        withContext(Dispatchers.Main) {
            when (shortcut.responseHandling?.uiType) {
                ResponseHandling.UI_TYPE_TOAST -> {
                    context.showToast(
                        (output ?: response?.getContentAsString(context) ?: "")
                            .truncate(maxLength = TOAST_MAX_LENGTH)
                            .let(HTMLUtil::toSpanned)
                            .ifBlank { context.getString(R.string.message_blank_response) },
                        long = shortcut.responseHandling?.successOutput == ResponseHandling.SUCCESS_OUTPUT_RESPONSE
                    )
                }
                ResponseHandling.UI_TYPE_DIALOG,
                null,
                -> {
                    showResultDialog(shortcut, response, output, dialogHandle)
                }
                ResponseHandling.UI_TYPE_WINDOW -> {
                    if (params.isNested) {
                        // When running in nested mode (i.e., the shortcut was invoked from another shortcut), we cannot open another activity
                        // because it would interrupt the execution. Therefore, we suppress it here.
                        return@withContext
                    }
                    val responseData = ResponseData(
                        shortcutId = shortcut.id,
                        shortcutName = shortcutName,
                        text = output,
                        mimeType = when (shortcut.responseHandling?.responseContentType) {
                            ResponseContentType.PLAIN_TEXT -> FileTypeUtil.TYPE_PLAIN_TEXT
                            ResponseContentType.JSON -> FileTypeUtil.TYPE_JSON
                            ResponseContentType.XML -> FileTypeUtil.TYPE_XML
                            ResponseContentType.HTML -> FileTypeUtil.TYPE_HTML
                            null -> response?.contentType
                        },
                        charset = response?.charset,
                        url = response?.url?.toUri(),
                        fileUri = response?.getContentUri(context),
                        statusCode = response?.statusCode,
                        headers = response?.headers?.toMultiMap() ?: emptyMap(),
                        timing = response?.timing,
                        showDetails = shortcut.responseHandling?.includeMetaInfo == true,
                        monospace = shortcut.responseHandling?.monospace == true,
                        fontSize = shortcut.responseHandling?.fontSize,
                        actions = shortcut.responseHandling?.displayActions ?: emptyList(),
                        jsonArrayAsTable = shortcut.responseHandling?.jsonArrayAsTable == true,
                    )
                    val responseDataId = navigationArgStore.storeArg(responseData)
                    DisplayResponseActivity.IntentBuilder(shortcutName, responseDataId)
                        .startActivity(context)
                }
                else -> Unit
            }
        }
    }

    private fun renameResponseFile(response: ShortcutResponse, variableManager: VariableManager, directoryUri: Uri) {
        try {
            val responseHandling = shortcut.responseHandling!!
            val directory = DocumentFile.fromTreeUri(context, directoryUri)
            val fileName = responseHandling.storeFileName
                ?.takeUnlessEmpty()
                ?.let {
                    Variables.rawPlaceholdersToResolvedValues(it, variableManager.getVariableValuesByIds())
                }
                ?: run {
                    response.contentDispositionFileName
                }
                ?: response.url.toUri().lastPathSegment
                ?: "http-response" // TODO: Better fallback

            if (responseHandling.replaceFileIfExists) {
                directory?.findFile(fileName)?.delete()
            }

            response.contentFile?.renameTo(fileName)
        } catch (e: Exception) {
            logError("Error while storing response to file: $e")
            logException(e)
        }
    }

    private fun logError(message: String) {
        historyEventLogger.logEvent(
            HistoryEvent.Error(
                shortcutName = if (::shortcut.isInitialized) shortcut.name else "???",
                error = message,
            )
        )
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ExecutionEntryPoint {
        fun shortcutRepository(): ShortcutRepository
        fun pendingExecutionsRepository(): PendingExecutionsRepository
        fun variableRepository(): VariableRepository
        fun appRepository(): AppRepository
        fun workingDirectoryRepository(): WorkingDirectoryRepository
        fun variableResolver(): VariableResolver
        fun httpRequester(): HttpRequester
        fun showResultDialog(): ShowResultDialogUseCase
        fun executionScheduler(): ExecutionScheduler
        fun launcherShortcutManager(): LauncherShortcutManager
        fun openInBrowser(): OpenInBrowserUseCase
        fun requestSimpleConfirmation(): RequestSimpleConfirmationUseCase
        fun requestBiometricConfirmation(): RequestBiometricConfirmationUseCase
        fun checkWifiSSID(): CheckWifiSSIDUseCase
        fun networkUtil(): NetworkUtil
        fun extractFileIdsFromVariableValues(): ExtractFileIdsFromVariableValuesUseCase
        fun scriptExecutor(): ScriptExecutor
        fun externalRequests(): ExternalRequests
        fun httpRequesterStarter(): HttpRequesterWorker.Starter
        fun checkHeadlessExecution(): CheckHeadlessExecutionUseCase
        fun errorFormatter(): ErrorFormatter
        fun historyEventLogger(): HistoryEventLogger
        fun cacheFilesCleanupStarter(): CacheFilesCleanupWorker.Starter
        fun historyCleanUpStarter(): HistoryCleanUpWorker.Starter
        fun executionSchedulerStarter(): ExecutionSchedulerWorker.Starter
        fun sessionMonitor(): SessionMonitor
        fun navigationArgStore(): NavigationArgStore
        fun validateRequestData(): ValidateRequestDataUseCase
    }

    companion object {
        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400
    }
}
