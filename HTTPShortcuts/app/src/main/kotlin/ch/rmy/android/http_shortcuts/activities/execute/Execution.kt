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
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.extensions.truncate
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
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.FileUploadOptions
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.extensions.isTemporaryShortcut
import ch.rmy.android.http_shortcuts.extensions.resolve
import ch.rmy.android.http_shortcuts.extensions.shouldIncludeInHistory
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.HttpRequesterWorker
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.scripting.ResultHandler
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Execution(
    private val context: Context,
    private val params: ExecutionParams,
    private val dialogHandle: DialogHandle,
) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    @Inject
    lateinit var variableResolver: VariableResolver

    @Inject
    lateinit var httpRequester: HttpRequester

    @Inject
    lateinit var showResultDialog: ShowResultDialogUseCase

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    @Inject
    lateinit var openInBrowser: OpenInBrowserUseCase

    @Inject
    lateinit var requestSimpleConfirmation: RequestSimpleConfirmationUseCase

    @Inject
    lateinit var requestBiometricConfirmation: RequestBiometricConfirmationUseCase

    @Inject
    lateinit var checkWifiSSID: CheckWifiSSIDUseCase

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var extractFileIdsFromVariableValues: ExtractFileIdsFromVariableValuesUseCase

    @Inject
    lateinit var scriptExecutor: ScriptExecutor

    @Inject
    lateinit var externalRequests: ExternalRequests

    @Inject
    lateinit var httpRequesterStarter: HttpRequesterWorker.Starter

    @Inject
    lateinit var checkHeadlessExecution: CheckHeadlessExecutionUseCase

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var historyEventLogger: HistoryEventLogger

    init {
        context.getApplicationComponent().inject(this)
    }

    private lateinit var globalCode: String
    private lateinit var shortcut: Shortcut
    private lateinit var certificatePins: List<CertificatePin>

    private val shortcutName by lazy {
        shortcut.getSafeName(context)
    }

    suspend fun execute(): Flow<ExecutionStatus> = flow {
        logInfo("Beginning to execute shortcut (${params.shortcutId}, trigger=${params.trigger ?: "unknown"})")
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
        }
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

        when (requiresConfirmation()) {
            ConfirmationType.SIMPLE -> requestSimpleConfirmation(shortcutName, dialogHandle)
            ConfirmationType.BIOMETRIC -> requestBiometricConfirmation(shortcutName)
            null -> Unit
        }
        checkWifiSSID(shortcutName, shortcut.wifiSsid, dialogHandle)

        val variableManager = VariableManager(variableRepository.getVariables(), params.variableValues)

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
            scriptExecutor.initialize(
                shortcut = shortcut,
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
                        shortcut,
                        sessionId = sessionId,
                        variableValues = variableManager.getVariableValuesByIds(),
                        fileUploadResult = fileUploadResult,
                        useCookieJar = shortcut.acceptCookies,
                        certificatePins = certificatePins,
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

        scriptExecutor.execute(
            script = shortcut.codeOnSuccess,
            response = response,
        )

        if (shortcut.responseHandling?.storeDirectory != null && response.contentFile != null) {
            renameResponseFile(response, variableManager)
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
            val baseDeferred = async {
                appRepository.getBase()
            }
            val shortcutDeferred = async {
                shortcutRepository.getShortcutById(params.shortcutId)
            }
            val base = baseDeferred.await()
            globalCode = base.globalCode.orEmpty()
            shortcut = shortcutDeferred.await()
            certificatePins = base.certificatePins
        }
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

    private fun openShortcutInBrowser(variableManager: VariableManager) {
        openInBrowser(url = injectVariables(shortcut.url, variableManager), browserPackageName = shortcut.browserPackageName)
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
                    DisplayResponseActivity.IntentBuilder(shortcut.id)
                        .name(shortcutName)
                        .type(response?.contentType)
                        .runIfNotNull(output) {
                            text(it)
                        }
                        .runIfNotNull(response?.getContentUri(context)) {
                            responseFileUri(it)
                        }
                        .runIfNotNull(response?.charset) {
                            charset(it)
                        }
                        .runIfNotNull(response?.url) {
                            url(it)
                        }
                        .runIf(shortcut.responseHandling?.includeMetaInfo == true) {
                            showDetails(true)
                                .timing(response?.timing)
                                .headers(response?.headers)
                                .statusCode(response?.statusCode)
                        }
                        .runIf(shortcut.responseHandling?.monospace == true) {
                            monospace()
                        }
                        .actions(shortcut.responseHandling?.displayActions ?: emptyList())
                        .startActivity(activityProvider.getActivity())
                }
                else -> Unit
            }
        }
    }

    private fun renameResponseFile(response: ShortcutResponse, variableManager: VariableManager) {
        try {
            val responseHandling = shortcut.responseHandling!!
            val directoryUri = responseHandling.storeDirectory!!.toUri()
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

    companion object {
        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400
    }
}
