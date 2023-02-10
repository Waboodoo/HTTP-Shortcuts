package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckHeadlessExecutionUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckWifiSSIDUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ExtractFileIdsFromVariableValuesUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.OpenInBrowserUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.PromptForConfirmationUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ShowResultDialogUseCase
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ShowShortcutNotFoundDialogUseCase
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.extensions.isTemporaryShortcut
import ch.rmy.android.http_shortcuts.extensions.resolve
import ch.rmy.android.http_shortcuts.extensions.shouldIncludeInHistory
import ch.rmy.android.http_shortcuts.extensions.showAndAwaitDismissal
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
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
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
    lateinit var promptForConfirmation: PromptForConfirmationUseCase

    @Inject
    lateinit var checkWifiSSID: CheckWifiSSIDUseCase

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var extractFileIdsFromVariableValues: ExtractFileIdsFromVariableValuesUseCase

    @Inject
    lateinit var showShortcutNotFoundDialog: ShowShortcutNotFoundDialogUseCase

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
    private lateinit var shortcut: ShortcutModel

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
            throw e
        } catch (e: Exception) {
            if (shortcut.shouldIncludeInHistory()) {
                historyEventLogger.logEvent(
                    HistoryEvent.Error(
                        shortcutName = shortcut.name,
                        error = "Unknown / unexpected error, please contact developer",
                    )
                )
            }
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
                    historyEventLogger.logEvent(
                        HistoryEvent.Error(
                            shortcutName = shortcut.name,
                            error = message,
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    try {
                        DialogBuilder(activityProvider.getActivity())
                            .title(R.string.dialog_title_error)
                            .message(message)
                            .positive(R.string.dialog_ok)
                            .showAndAwaitDismissal()
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
            showShortcutNotFoundDialog()
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

        if (requiresConfirmation()) {
            promptForConfirmation(shortcutName)
        }
        checkWifiSSID(shortcutName, shortcut.wifiSsid)

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

        if ((params.tryNumber == 0 || (params.tryNumber == 1 && shortcut.delay > 0)) && usesScripting) {
            scriptExecutor.initialize(
                shortcut = shortcut,
                variableManager = variableManager,
                fileUploadResult = fileUploadResult,
                resultHandler = resultHandler,
                recursionDepth = params.recursionDepth,
            )
            scriptExecutor.execute(globalCode)
            scriptExecutor.execute(shortcut.codeOnPrepare)
        }

        variableResolver.resolve(variableManager, shortcut)

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
                    )
            } catch (e: UnknownHostException) {
                if (shouldReschedule(e)) {
                    if (shortcut.responseHandling?.successOutput != ResponseHandlingModel.SUCCESS_OUTPUT_NONE && params.tryNumber == 0) {
                        context.showToast(
                            String.format(
                                context.getString(R.string.execution_delayed),
                                shortcutName,
                            ),
                            long = true,
                        )
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
                    ResponseHandlingModel.FAILURE_OUTPUT_DETAILED,
                    ResponseHandlingModel.FAILURE_OUTPUT_SIMPLE,
                    -> {
                        displayResult(
                            generateOutputFromError(e, simple = failureOutput == ResponseHandlingModel.FAILURE_OUTPUT_SIMPLE),
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
            val globalCodeDeferred = async {
                appRepository.getGlobalCode()
            }
            val shortcutDeferred = async {
                shortcutRepository.getShortcutById(params.shortcutId)
            }
            globalCode = globalCodeDeferred.await()
            shortcut = shortcutDeferred.await()
        }
    }

    private suspend fun scheduleRepetitionIfNeeded() {
        if (shortcut.isTemporaryShortcut) {
            return
        }
        val repetition = shortcut.repetition ?: return
        pendingExecutionsRepository
            .createPendingExecution(
                shortcutId = shortcut.id,
                delay = repetition.interval.minutes,
                requiresNetwork = false,
                type = PendingExecutionType.REPEAT,
            )
    }

    private fun requiresConfirmation() =
        shortcut.requireConfirmation && params.tryNumber == 0

    private suspend fun handleFiles(loadMetaData: Boolean): FileUploadManager.Result? = coroutineScope {
        if (!shortcut.usesRequestParameters() && !shortcut.usesGenericFileBody() && !shortcut.usesImageFileBody()) {
            return@coroutineScope null
        }

        val fileUploadManager = FileUploadManager.Builder(context.contentResolver)
            .withSharedFiles(params.fileUris)
            .withForwardedFiles(extractFileIdsFromVariableValues(params.variableValues))
            .runIf(shortcut.usesGenericFileBody()) {
                addFileRequest()
            }
            .runIf(shortcut.usesImageFileBody()) {
                addFileRequest(image = true)
            }
            .runFor(shortcut.parameters) { parameter ->
                when (parameter.parameterType) {
                    ParameterType.STRING -> this
                    ParameterType.FILE -> addFileRequest(multiple = false)
                    ParameterType.FILES -> addFileRequest(multiple = true)
                    ParameterType.IMAGE -> addFileRequest(image = true)
                }
            }
            .withMetaData(loadMetaData)
            .build()

        var fileRequest: FileUploadManager.FileRequest
        while (true) {
            fileRequest = fileUploadManager.getNextFileRequest() ?: break
            ensureActive()
            if (fileRequest.image) {
                fileUploadManager.fulfillFileRequest(externalRequests.openCamera())
            } else {
                fileUploadManager.fulfillFileRequest(externalRequests.openFilePicker(fileRequest.multiple))
            }
        }

        fileUploadManager.getResult()
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
            ResponseHandlingModel.SUCCESS_OUTPUT_MESSAGE -> {
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
            ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE -> displayResult(output = null, response)
        }
    }

    private suspend fun displayResult(output: String?, response: ShortcutResponse? = null) {
        withContext(Dispatchers.Main) {
            when (shortcut.responseHandling?.uiType) {
                ResponseHandlingModel.UI_TYPE_TOAST -> {
                    context.showToast(
                        (output ?: response?.getContentAsString(context) ?: "")
                            .truncate(maxLength = TOAST_MAX_LENGTH)
                            .let(HTMLUtil::format)
                            .ifBlank { context.getString(R.string.message_blank_response) },
                        long = shortcut.responseHandling?.successOutput == ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE
                    )
                }
                ResponseHandlingModel.UI_TYPE_DIALOG,
                null,
                -> {
                    showResultDialog(shortcut, response, output)
                }
                ResponseHandlingModel.UI_TYPE_WINDOW -> {
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
                        .runIfNotNull(response?.contentFile) {
                            responseFileUri(it)
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
                        .actions(shortcut.responseHandling?.displayActions ?: emptyList())
                        .startActivity(activityProvider.getActivity())
                }
                else -> Unit
            }
        }
    }

    companion object {
        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400
    }
}
