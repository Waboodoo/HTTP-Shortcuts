package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.utils.DateUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
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
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.extensions.showAndAwaitDismissal
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.ResponseFileStorage
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.math.pow

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

    init {
        context.getApplicationComponent().inject(this)
    }

    private lateinit var globalCode: String
    private lateinit var shortcut: ShortcutModel

    private val shortcutName by lazy {
        shortcut.getSafeName(context)
    }

    suspend fun execute(): Flow<Status> = flow {
        emit(Status.PREPARING)
        try {
            executeWithoutExceptionHandling()
        } catch (e: UserException) {
            displayError(e)
        } catch (e: NoActivityAvailableException) {
            throw CancellationException("Host activity disappeared, cancelling", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!isExpected(e)) {
                logException(e)
            }

            coroutineScope {
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
            }
        }
    }

    private suspend fun displayError(error: Throwable) {
        generateOutputFromError(error)
            .let { message ->
                DialogBuilder(activityProvider.getActivity())
                    .title(R.string.dialog_title_error)
                    .message(message)
                    .positive(R.string.dialog_ok)
                    .showAndAwaitDismissal()
            }
    }

    private fun generateOutputFromError(error: Throwable, simple: Boolean = false) =
        ErrorFormatter(context).getPrettyError(error, shortcutName, includeBody = !simple)

    private suspend fun FlowCollector<Status>.executeWithoutExceptionHandling() {
        if (params.executionId != null) {
            pendingExecutionsRepository.removePendingExecution(params.executionId)
        }

        try {
            loadData()
        } catch (e: NoSuchElementException) {
            showShortcutNotFoundDialog()
            return
        }

        if (requiresConfirmation()) {
            promptForConfirmation(shortcutName)
        }
        checkWifiSSID(shortcutName, shortcut.wifiSsid)

        val variableManager = resolveVariables()

        if (shouldDelayExecution()) {
            pendingExecutionsRepository.createPendingExecution(
                shortcutId = shortcut.id,
                resolvedVariables = variableManager.getVariableValuesByKeys(),
                waitUntil = DateUtil.calculateDate(shortcut.delay),
                tryNumber = 1,
                recursionDepth = params.recursionDepth,
                requiresNetwork = shortcut.isWaitForNetwork,
            )
            return
        }

        val fileUploadResult = handleFiles()

        emit(Status.IN_PROGRESS)

        if ((params.tryNumber == 0 || (params.tryNumber == 1 && shortcut.delay > 0)) && usesScripting()) {
            scriptExecutor.initialize(
                shortcut = shortcut,
                variableManager = variableManager,
                fileUploadResult = fileUploadResult,
                recursionDepth = params.recursionDepth,
            )
            scriptExecutor.execute(globalCode)
            scriptExecutor.execute(shortcut.codeOnPrepare)
        }

        when (shortcut.type) {
            ShortcutExecutionType.BROWSER -> {
                openShortcutInBrowser(variableManager)
                return
            }
            ShortcutExecutionType.SCRIPTING,
            ShortcutExecutionType.TRIGGER,
            -> {
                // nothing more to do
                return
            }
            ShortcutExecutionType.APP -> {
                // continue
            }
        }

        // TODO: Check if the HTTP request can be run headless, and if so, run it in a worker and return early
        // headless iff: response type is none or toast & no codeOnFailure and no codeOnSuccess
        val response = try {
            httpRequester
                .executeShortcut(
                    context,
                    shortcut,
                    variableValues = variableManager.getVariableValuesByIds(),
                    ResponseFileStorage(context, shortcut.id), // TODO: Create an injectable factory for this, instead pass only a session-id in here
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
                throw CancellationException()
            }
            throw e
        } catch (e: Exception) {
            if (e is ErrorResponse || e is IOException) {
                scriptExecutor.execute(
                    script = shortcut.codeOnFailure,
                    error = e,
                )
            }
            throw e
        }

        scriptExecutor.execute(
            script = shortcut.codeOnSuccess,
            response = response,
        )

        emit(Status.WRAPPING_UP)

        handleDisplayingOfResult(response, variableManager)
    }

    private fun shouldReschedule(error: Throwable): Boolean =
        shortcut.isWaitForNetwork &&
            error !is ErrorResponse &&
            !networkUtil.isNetworkConnected()

    private suspend fun rescheduleExecution(variableManager: VariableManager) {
        if (params.tryNumber < MAX_RETRY) {
            val waitUntil = DateUtil.calculateDate(calculateDelay())
            pendingExecutionsRepository
                .createPendingExecution(
                    shortcutId = shortcut.id,
                    resolvedVariables = variableManager.getVariableValuesByKeys(),
                    tryNumber = params.tryNumber + 1,
                    waitUntil = waitUntil,
                    recursionDepth = params.recursionDepth,
                    requiresNetwork = shortcut.isWaitForNetwork,
                )
        }
    }

    private fun calculateDelay() =
        RETRY_BACKOFF.pow(params.tryNumber.toDouble()).toInt() * 1000

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

    private fun requiresConfirmation() =
        shortcut.requireConfirmation && params.tryNumber == 0

    private suspend fun resolveVariables(): VariableManager =
        variableResolver.resolve(
            variables = variableRepository.getVariables(),
            shortcut = shortcut,
            preResolvedValues = params.variableValues,
        )

    private suspend fun handleFiles(): FileUploadManager.Result? = coroutineScope {
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
            .build()

        var fileRequest: FileUploadManager.FileRequest
        while (true) {
            fileRequest = fileUploadManager.getNextFileRequest() ?: break
            ensureActive()
            if (fileRequest.image) {
                fileUploadManager.fulfilFileRequest(externalRequests.openCamera())
            } else {
                fileUploadManager.fulfilFileRequest(externalRequests.openFilePicker(fileRequest.multiple))
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
        when (shortcut.responseHandling!!.successOutput) {
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
            ResponseHandlingModel.SUCCESS_OUTPUT_NONE -> Unit
        }
    }

    private suspend fun displayResult(output: String?, response: ShortcutResponse? = null) {
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

    enum class Status {
        PREPARING,
        IN_PROGRESS,
        WRAPPING_UP,
    }

    companion object {
        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400

        private fun isExpected(throwable: Throwable?) =
            throwable is ErrorResponse || throwable is IOException
    }
}
