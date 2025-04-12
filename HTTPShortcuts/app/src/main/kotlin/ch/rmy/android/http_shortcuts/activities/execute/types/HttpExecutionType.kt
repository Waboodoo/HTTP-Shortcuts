package ch.rmy.android.http_shortcuts.activities.execute.types

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckHeadlessExecutionUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ShowResultDialogUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ShowResultNotificationUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ValidateRequestDataUseCase
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.activities.response.models.ResponseData
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType.SCHEDULE_IMMEDIATELY
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.TreatAsFailureException
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.HttpRequesterWorker
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.scripting.ResultHandler
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class HttpExecutionType
@Inject
constructor(
    private val context: Context,
    private val httpRequester: HttpRequester,
    private val httpRequesterStarter: HttpRequesterWorker.Starter,
    private val executionScheduler: ExecutionScheduler,
    private val checkHeadlessExecution: CheckHeadlessExecutionUseCase,
    private val certificatePinRepository: CertificatePinRepository,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val workingDirectoryRepository: WorkingDirectoryRepository,
    private val validateRequestData: ValidateRequestDataUseCase,
    private val networkUtil: NetworkUtil,
    private val historyEventLogger: HistoryEventLogger,
    private val errorFormatter: ErrorFormatter,
    private val showResultDialog: ShowResultDialogUseCase,
    private val showResultNotification: ShowResultNotificationUseCase,
    private val navigationArgStore: NavigationArgStore,
) : ExecutionType() {

    override fun invoke(
        params: ExecutionParams,
        shortcut: Shortcut,
        requestHeaders: List<RequestHeader>,
        requestParameters: List<RequestParameter>,
        variableManager: VariableManager,
        resultHandler: ResultHandler,
        fileUploadResult: FileUploadManager.Result?,
        dialogHandle: DialogHandle,
        scriptExecutor: ScriptExecutor,
    ): Flow<ExecutionStatus> = flow {
        val sessionId = "${shortcut.id}_${newUUID()}"
        if ((params.recursionDepth == 0 || params.trigger == SCHEDULE_IMMEDIATELY) && checkHeadlessExecution(
                shortcut,
                requestParameters,
                variableManager.getVariableValuesByIds(),
            )
        ) {
            logInfo("Preparing to execute HTTP request in headless mode")
            try {
                httpRequesterStarter.invoke(
                    shortcutId = shortcut.id,
                    sessionId = sessionId,
                    variableValues = variableManager.getVariableValuesByIds(),
                    fileUploadResult = fileUploadResult,
                )
                return@flow
            } catch (e: Throwable) {
                logException(e)
            }
        }

        val workingDirectory = shortcut.responseStoreDirectoryId?.let { workingDirectoryId ->
            try {
                workingDirectoryRepository.getWorkingDirectoryById(workingDirectoryId)
            } catch (_: NoSuchElementException) {
                null
            }
        }

        val response = try {
            try {
                httpRequester
                    .executeShortcut(
                        context,
                        shortcut = shortcut,
                        headers = requestHeaders,
                        parameters = requestParameters,
                        storeDirectoryUri = workingDirectory?.directory,
                        sessionId = sessionId,
                        variableValues = variableManager.getVariableValuesByIds(),
                        fileUploadResult = fileUploadResult,
                        useCookieJar = shortcut.acceptCookies,
                        certificatePins = certificatePinRepository.getCertificatePins(),
                        validateRequestData = { requestData ->
                            validateRequestData(dialogHandle, shortcut, requestData)
                        },
                    )
            } catch (e: UnknownHostException) {
                if (shouldReschedule(shortcut, e)) {
                    if (shortcut.responseSuccessOutput != ResponseSuccessOutput.NONE && params.tryNumber == 0) {
                        withContext(Dispatchers.Main) {
                            context.showToast(
                                String.format(
                                    context.getString(R.string.execution_delayed),
                                    shortcut.getSafeName(context),
                                ),
                                long = true,
                            )
                        }
                    }
                    rescheduleExecution(shortcut, params, variableManager)
                    executionScheduler.schedule()
                    return@flow
                }
                throw e
            }
        } catch (e: Exception) {
            if (e is ErrorResponse || e is IOException) {
                scriptExecutor.execute(
                    script = shortcut.codeOnFailure,
                    error = e,
                )

                when (val failureOutput = shortcut.responseFailureOutput) {
                    ResponseFailureOutput.DETAILED,
                    ResponseFailureOutput.SIMPLE,
                    -> {
                        displayResult(
                            shortcut = shortcut,
                            params = params,
                            dialogHandle = dialogHandle,
                            output = generateOutputFromError(
                                shortcut = shortcut,
                                error = e,
                                simple = failureOutput == ResponseFailureOutput.SIMPLE,
                            ),
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
                return@flow
            }
            throw e
        }

        try {
            scriptExecutor.execute(
                script = shortcut.codeOnSuccess,
                response = response,
            )
        } catch (e: TreatAsFailureException) {
            scriptExecutor.execute(
                script = shortcut.codeOnFailure,
                error = ErrorResponse(response),
            )

            when (val failureOutput = shortcut.responseFailureOutput) {
                ResponseFailureOutput.DETAILED,
                ResponseFailureOutput.SIMPLE,
                -> {
                    displayResult(
                        params = params,
                        shortcut = shortcut,
                        dialogHandle = dialogHandle,
                        output = generateOutputFromError(
                            shortcut = shortcut,
                            error = e,
                            simple = failureOutput == ResponseFailureOutput.SIMPLE,
                        ),
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
            return@flow
        }

        if (shortcut.responseStoreDirectoryId != null && response.contentFile != null) {
            workingDirectoryRepository.touchWorkingDirectory(shortcut.responseStoreDirectoryId)
            withContext(Dispatchers.IO) {
                workingDirectory?.directory?.let {
                    renameResponseFile(shortcut, response, variableManager, it)
                }
            }
        }

        emit(
            ExecutionStatus.WrappingUp(
                variableManager.getVariableValuesByIds(),
                result = resultHandler.getResult(),
            ),
        )
        handleDisplayingOfResult(
            shortcut = shortcut,
            params = params,
            dialogHandle = dialogHandle,
            response = response,
            variableManager = variableManager,
        )
        logInfo("Execution completed successfully (${params.shortcutId})")
        emit(
            ExecutionStatus.CompletedSuccessfully(
                response = response,
                variableValues = variableManager.getVariableValuesByIds(),
                result = resultHandler.getResult(),
            ),
        )
    }

    private fun generateOutputFromError(
        shortcut: Shortcut,
        error: Throwable,
        simple: Boolean = false,
    ): String =
        errorFormatter.getPrettyError(error, shortcut.getSafeName(context), includeBody = !simple)

    private fun shouldReschedule(
        shortcut: Shortcut,
        error: Throwable,
    ): Boolean =
        shortcut.isWaitForNetwork &&
            error !is ErrorResponse &&
            !networkUtil.isNetworkConnected()

    private suspend fun rescheduleExecution(
        shortcut: Shortcut,
        params: ExecutionParams,
        variableManager: VariableManager,
    ) {
        if (params.tryNumber < MAX_RETRY) {
            pendingExecutionsRepository
                .createPendingExecution(
                    shortcutId = shortcut.id,
                    resolvedVariables = variableManager.getVariableValuesByKeys(),
                    tryNumber = params.tryNumber + 1,
                    delay = calculateDelay(params),
                    recursionDepth = params.recursionDepth,
                    requiresNetwork = shortcut.isWaitForNetwork,
                    type = PendingExecutionType.RETRY_LATER,
                )
        }
    }

    private fun calculateDelay(params: ExecutionParams) =
        (RETRY_BACKOFF.pow(params.tryNumber.toDouble()).toInt()).seconds

    private suspend fun handleDisplayingOfResult(
        shortcut: Shortcut,
        params: ExecutionParams,
        dialogHandle: DialogHandle,
        response: ShortcutResponse,
        variableManager: VariableManager,
    ) {
        val output = when (shortcut.responseSuccessOutput) {
            ResponseSuccessOutput.MESSAGE -> {
                shortcut.responseSuccessMessage
                    .takeUnlessEmpty()
                    ?.let {
                        injectVariables(it, variableManager)
                    }
                    ?: context.getString(R.string.executed, shortcut.getSafeName(context))
            }
            ResponseSuccessOutput.RESPONSE -> null
            else -> return
        }
        displayResult(
            shortcut = shortcut,
            params = params,
            dialogHandle = dialogHandle,
            output = output,
            response = response,
        )
    }

    private suspend fun displayResult(
        shortcut: Shortcut,
        params: ExecutionParams,
        dialogHandle: DialogHandle,
        output: String?,
        response: ShortcutResponse? = null,
    ) {
        withContext(Dispatchers.Main) {
            when (shortcut.responseUiType) {
                ResponseUiType.TOAST -> {
                    context.showToast(
                        (output ?: response?.getContentAsString(context) ?: "")
                            .truncate(maxLength = TOAST_MAX_LENGTH)
                            .let(HTMLUtil::toSpanned)
                            .ifBlank { context.getString(R.string.message_blank_response) },
                        long = shortcut.responseSuccessOutput == ResponseSuccessOutput.RESPONSE,
                    )
                }
                ResponseUiType.NOTIFICATION -> {
                    showResultNotification(shortcut, response, output)
                }
                ResponseUiType.DIALOG -> {
                    showResultDialog(shortcut, response, output, dialogHandle)
                }
                ResponseUiType.WINDOW -> {
                    if (params.isNested) {
                        // When running in nested mode (i.e., the shortcut was invoked from another shortcut), we cannot open another activity
                        // because it would interrupt the execution. Therefore, we suppress it here.
                        return@withContext
                    }
                    val responseData = ResponseData(
                        shortcutId = shortcut.id,
                        title = shortcut.getSafeName(context),
                        text = output,
                        mimeType = when (shortcut.responseContentType) {
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
                        showDetails = shortcut.responseIncludeMetaInfo,
                        monospace = shortcut.responseMonospace,
                        fontSize = shortcut.responseFontSize,
                        actions = shortcut.responseDisplayActions,
                        jsonArrayAsTable = shortcut.responseJsonArrayAsTable,
                        javaScriptEnabled = shortcut.responseJavaScriptEnabled,
                    )
                    val responseDataId = navigationArgStore.storeArg(responseData)
                    DisplayResponseActivity.IntentBuilder(title = shortcut.getSafeName(context), responseDataId)
                        .startActivity(context)
                }
            }
        }
    }

    private fun renameResponseFile(
        shortcut: Shortcut,
        response: ShortcutResponse,
        variableManager: VariableManager,
        directoryUri: Uri,
    ) {
        try {
            val directory = DocumentFile.fromTreeUri(context, directoryUri)
            val fileName = shortcut.responseStoreFileName
                ?.takeUnlessEmpty()
                ?.let {
                    Variables.rawPlaceholdersToResolvedValues(it, variableManager.getVariableValuesByIds())
                }
                ?: run {
                    response.contentDispositionFileName
                }
                ?: response.url.toUri().lastPathSegment
                ?: "http-response" // TODO: Better fallback

            if (shortcut.responseReplaceFileIfExists) {
                directory?.findFile(fileName)?.delete()
            }

            response.contentFile?.renameTo(fileName)
        } catch (e: Exception) {
            logError(shortcut, "Error while storing response to file: $e")
            logException(e)
        }
    }

    private fun logError(shortcut: Shortcut, message: String) {
        historyEventLogger.logEvent(
            HistoryEvent.Error(
                shortcutName = shortcut.getSafeName(context),
                error = message,
            ),
        )
    }

    companion object {
        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400
    }
}
