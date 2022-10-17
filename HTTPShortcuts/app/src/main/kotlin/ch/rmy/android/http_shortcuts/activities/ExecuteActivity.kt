package ch.rmy.android.http_shortcuts.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.activity.result.launch
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.doOnDestroy
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.utils.DateUtil
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteEvent
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteViewModel
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.databinding.DialogTextBinding
import ch.rmy.android.http_shortcuts.exceptions.BrowserNotFoundException
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.exceptions.MissingLocationPermissionException
import ch.rmy.android.http_shortcuts.exceptions.NoActivityAvailableException
import ch.rmy.android.http_shortcuts.exceptions.ResumeLaterException
import ch.rmy.android.http_shortcuts.exceptions.UnsupportedFeatureException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.canceledByUser
import ch.rmy.android.http_shortcuts.extensions.loadImage
import ch.rmy.android.http_shortcuts.extensions.reloadImageSpans
import ch.rmy.android.http_shortcuts.extensions.showAndAwaitDismissal
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.ResponseFileStorage
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.plugin.SessionMonitor
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.scheduling.ExecutionsWorker
import ch.rmy.android.http_shortcuts.scripting.ActionRequest
import ch.rmy.android.http_shortcuts.scripting.ActionResult
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.scripting.actions.ActionFactory
import ch.rmy.android.http_shortcuts.utils.BarcodeScannerContract
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil.isImage
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import ch.rmy.android.http_shortcuts.utils.ProgressIndicator
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.CookieJar
import java.io.IOException
import java.net.UnknownHostException
import java.util.HashMap
import javax.inject.Inject
import kotlin.math.pow

class ExecuteActivity : BaseActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    override val supportsSnackbars: Boolean
        get() = false

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    @Inject
    lateinit var httpRequester: HttpRequester

    @Inject
    lateinit var variableResolver: VariableResolver

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    @Inject
    lateinit var permissionManager: PermissionManager

    private val viewModel: ExecuteViewModel by bindViewModel()

    /* TODO: Get rid of these fields */
    private lateinit var shortcut: ShortcutModel
    private lateinit var variableValues: Map<String, String>
    private lateinit var fileIds: List<String>
    private lateinit var globalCode: String

    private val progressIndicator: ProgressIndicator by lazy {
        ProgressIndicator(this)
            .also { progressIndicator ->
                doOnDestroy {
                    progressIndicator.destroy()
                }
            }
    }

    private val scriptExecutor: ScriptExecutor by lazy {
        ScriptExecutor(context, ActionFactory(), sendRequest = ::onActionRequest)
    }

    /* Execution Parameters */
    private val tryNumber by lazy {
        intent.extras?.getInt(EXTRA_TRY_NUMBER) ?: 0
    }
    private val recursionDepth by lazy {
        intent.extras?.getInt(EXTRA_RECURSION_DEPTH) ?: 0
    }
    private val fileUris: List<Uri> by lazy {
        intent.extras?.getParcelableArrayList<Uri>(EXTRA_FILES) ?: emptyList<Uri>()
    }
    private val shortcutName by lazy {
        shortcut.name.ifEmpty { getString(R.string.shortcut_safe_name) }
    }

    private val pickFiles = registerForActivityResult(FilePickerUtil.PickFiles) { files ->
        if (files != null) {
            resumeAfterFileRequest(fileUris = files)
        } else {
            finishWithoutAnimation()
        }
    }
    private val openCamera = registerForActivityResult(FilePickerUtil.OpenCamera) { resultCallback ->
        resultCallback.invoke(this)
            ?.let { file ->
                resumeAfterFileRequest(fileUris = listOf(file))
            }
            ?: finishWithoutAnimation()
    }
    private val scanBarcode = registerForActivityResult(BarcodeScannerContract) { result ->
        scriptExecutor.pushActionResult(
            if (result != null) {
                ActionResult.ScanBarcodeResult.Success(result)
            } else {
                ActionResult.Cancelled
            }
        )
    }

    /* Caches / State */
    private var fileUploadManager: FileUploadManager? = null
    private lateinit var variableManager: VariableManager
    private val cookieJar: CookieJar by lazy {
        CookieManager.getCookieJar(context)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        lifecycleScope.launch {
            pendingExecutionsRepository
                .createPendingExecution(
                    shortcutId = IntentUtil.getShortcutId(intent),
                    resolvedVariables = IntentUtil.getVariableValues(intent),
                    tryNumber = 0,
                )
        }
    }

    override fun onCreated(savedState: Bundle?) {
        getApplicationComponent().inject(this)
        SessionMonitor.onSessionStarted()

        viewModel.initialize(
            ExecuteViewModel.InitData(
                shortcutId = IntentUtil.getShortcutId(intent),
                variableValues = IntentUtil.getVariableValues(intent),
                executionId = intent.extras?.getString(EXTRA_EXECUTION_SCHEDULE_ID),
            )
        )

        initViewModelBindings()
    }

    private fun initViewModelBindings() {
        // TODO: Factor this out of the activity as much as possible, into the viewmodel and perhaps parts into a service or worker
        lifecycleScope.launch {
            viewModel.viewState.collectLatest { viewState ->
                setDialogState(viewState.dialogState, viewModel)
            }
        }
        lifecycleScope.launch {
            viewModel.events.collect(::handleEvent)
        }
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ExecuteEvent.Execute -> {
                this.shortcut = event.shortcut
                this.variableValues = event.variableValues
                this.fileIds = event.fileIds
                this.globalCode = event.globalCode
                onDataLoaded()
            }
            else -> super.handleEvent(event)
        }
    }

    private fun onDataLoaded() {
        if (isFinishing) {
            return
        }
        setTheme(themeHelper.transparentTheme)

        doOnDestroy {
            if (fileUploadManager != null) {
                FileUtil.deleteOldCacheFiles(context)
            }
            ExecutionsWorker.schedule(context)
        }

        lifecycleScope.launch {
            subscribeAndFinishAfterIfNeeded {
                promptForConfirmationIfNeeded()
                resolveVariablesAndExecute()
            }
        }
    }

    @SuppressLint("CheckResult")
    private suspend fun subscribeAndFinishAfterIfNeeded(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResumeLaterException) {
            return
        } catch (e: NoActivityAvailableException) {
            // do nothing
        } catch (e: CanceledByUserException) {
            // do nothing
        } catch (e: UserException) {
            displayError(e)
        } catch (e: Exception) {
            if (!isExpected(e)) {
                logException(e)
            }

            if (shouldReschedule(e)) {
                if (shortcut.responseHandling?.successOutput != ResponseHandlingModel.SUCCESS_OUTPUT_NONE && tryNumber == 0) {
                    showToast(
                        String.format(
                            context.getString(R.string.execution_delayed),
                            shortcutName,
                        ),
                        long = true,
                    )
                }
                rescheduleExecution()
                executionScheduler.schedule()
            } else {
                when (shortcut.responseHandling?.failureOutput) {
                    ResponseHandlingModel.FAILURE_OUTPUT_DETAILED -> {
                        displayOutput(
                            generateOutputFromError(e, simple = false),
                            response = (e as? ErrorResponse)?.shortcutResponse,
                        )
                    }
                    ResponseHandlingModel.FAILURE_OUTPUT_SIMPLE -> {
                        displayOutput(
                            generateOutputFromError(e, simple = true),
                            response = (e as? ErrorResponse)?.shortcutResponse,
                        )
                    }
                    else -> Unit
                }
            }
        }
        finishWithoutAnimation()
    }

    private fun requiresConfirmation() =
        shortcut.requireConfirmation && tryNumber == 0

    private fun shouldDelayExecution() =
        shortcut.delay > 0 && tryNumber == 0

    private fun shouldReschedule(error: Throwable): Boolean =
        shortcut.isWaitForNetwork &&
            error !is ErrorResponse &&
            (error is UnknownHostException || !NetworkUtil.isNetworkConnected(context))

    private suspend fun promptForConfirmationIfNeeded() {
        if (requiresConfirmation()) {
            promptForConfirmation()
        }
    }

    private suspend fun promptForConfirmation() {
        suspendCancellableCoroutine<Unit> { continuation ->
            DialogBuilder(context)
                .title(shortcutName)
                .message(R.string.dialog_message_confirm_shortcut_execution)
                .dismissListener {
                    continuation.canceledByUser()
                }
                .positive(R.string.dialog_ok) {
                    continuation.resume()
                }
                .negative(R.string.dialog_cancel)
                .showOrElse {
                    continuation.canceledByUser()
                }
        }
    }

    private suspend fun displayError(error: Throwable) {
        generateOutputFromError(error)
            .let { message ->
                if (isFinishing) {
                    showToast(message, long = true)
                } else {
                    DialogBuilder(context)
                        .title(R.string.dialog_title_error)
                        .message(message)
                        .positive(R.string.dialog_ok)
                        .showAndAwaitDismissal()
                }
            }
    }

    private suspend fun resolveVariablesAndExecute() {
        val variables = variableRepository.getVariables()
        val variableManager = variableResolver.resolve(
            variables,
            shortcut,
            variableValues,
        )
        this.variableManager = variableManager
        if (shouldDelayExecution()) {
            val waitUntil = DateUtil.calculateDate(shortcut.delay)
            pendingExecutionsRepository.createPendingExecution(
                shortcutId = shortcut.id,
                resolvedVariables = variableManager.getVariableValuesByKeys(),
                waitUntil = waitUntil,
                tryNumber = 1,
                recursionDepth = recursionDepth,
                requiresNetwork = shortcut.isWaitForNetwork,
            )
        } else {
            executeWithFileRequests()
        }
    }

    private fun createFileUploadManagerIfNeeded() {
        if (fileUploadManager != null || (!shortcut.usesRequestParameters() && !shortcut.usesGenericFileBody() && !shortcut.usesImageFileBody())) {
            return
        }
        fileUploadManager = FileUploadManager.Builder(contentResolver)
            .withSharedFiles(fileUris)
            .withForwardedFiles(fileIds)
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
    }

    private suspend fun executeWithFileRequests() {
        createFileUploadManagerIfNeeded()
        val fileRequest = fileUploadManager?.getNextFileRequest()
        return if (fileRequest == null) {
            executeWithActions()
        } else {
            openFilePickerForFileParameter(multiple = fileRequest.multiple, image = fileRequest.image)
        }
    }

    private fun openFilePickerForFileParameter(multiple: Boolean, image: Boolean) {
        try {
            if (image) {
                logInfo("Opening camera for form parameter / request parameter")
                openCamera.launch()
            } else {
                logInfo("Opening file picker for form parameter / request parameter")
                pickFiles.launch(multiple)
            }
            throw ResumeLaterException()
        } catch (e: ActivityNotFoundException) {
            throw UnsupportedFeatureException()
        }
    }

    private suspend fun checkWifiNetworkSsid() {
        if (shortcut.wifiSsid.isEmpty() || NetworkUtil.getCurrentSsid(context).orEmpty() == shortcut.wifiSsid) {
            showProgress()
        } else {
            showWifiPickerConfirmation()
        }
    }

    private suspend fun showWifiPickerConfirmation() {
        DialogBuilder(context)
            .title(shortcutName)
            .message(getString(R.string.message_wrong_wifi_network, shortcut.wifiSsid))
            .positive(R.string.action_label_select) {
                NetworkUtil.showWifiPicker(this)
            }
            .negative(R.string.dialog_cancel)
            .showAndAwaitDismissal()
        throw CanceledByUserException()
    }

    private suspend fun showRequestPermissionRationalIfNeeded() {
        if (permissionManager.shouldShowRationaleForLocationPermission()) {
            DialogBuilder(context)
                .title(R.string.title_permission_dialog)
                .message(R.string.message_permission_rational)
                .positive(R.string.dialog_ok)
                .showAndAwaitDismissal()
        }
    }

    private suspend fun requestPermissionsForWifiCheck() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw MissingLocationPermissionException()
        }
    }

    private suspend fun executeWithActions() {
        requestPermissionsForWifiCheckIfNeeded()
        checkWifiNetworkSsid()

        if ((tryNumber == 0 || (tryNumber == 1 && shortcut.delay > 0)) && usesScripting()) {
            scriptExecutor.initialize(
                shortcut = shortcut,
                variableManager = variableManager,
                fileUploadManager = fileUploadManager,
                recursionDepth = recursionDepth,
            )
            scriptExecutor.execute(
                script = globalCode,
            )
            scriptExecutor.execute(
                script = shortcut.codeOnPrepare,
            )
        }

        when (shortcut.type) {
            ShortcutExecutionType.APP -> executeShortcut()
            ShortcutExecutionType.BROWSER -> openShortcutInBrowser()
            else -> Unit
        }
    }

    private suspend fun requestPermissionsForWifiCheckIfNeeded() {
        if (shortcut.wifiSsid.isNotEmpty()) {
            showRequestPermissionRationalIfNeeded()
            requestPermissionsForWifiCheck()
        }
    }

    private fun onActionRequest(request: ActionRequest) {
        when (request) {
            ActionRequest.ScanBarcode -> openBarcodeScanner()
        }
    }

    private fun openBarcodeScanner() {
        try {
            scanBarcode.launch()
        } catch (e: ActivityNotFoundException) {
            scriptExecutor.pushActionResult(ActionResult.ScanBarcodeResult.ScannerAppNotInstalled)
        }
    }

    private fun usesScripting() =
        shortcut.codeOnPrepare.isNotEmpty() ||
            shortcut.codeOnSuccess.isNotEmpty() ||
            shortcut.codeOnFailure.isNotEmpty() ||
            globalCode.isNotEmpty()

    private fun openShortcutInBrowser() {
        val url = injectVariables(shortcut.url)
        try {
            val uri = url.toUri()
            if (!Validation.isValidUrl(uri)) {
                throw InvalidUrlException(url)
            }
            if (uri.scheme?.equals("file", ignoreCase = true) == true) {
                // TODO: Localize error message
                throw UserException.create {
                    "URLs with file:// scheme are not supported"
                }
            }
            Intent(Intent.ACTION_VIEW, uri)
                .runIf(shortcut.browserPackageName.isNotEmpty()) {
                    setPackage(shortcut.browserPackageName)
                }
                .startActivity(this)
        } catch (e: ActivityNotFoundException) {
            if (shortcut.browserPackageName.isNotEmpty()) {
                throw BrowserNotFoundException(shortcut.browserPackageName)
            }
            throw UnsupportedFeatureException()
        }
    }

    private fun injectVariables(string: String): String =
        Variables.rawPlaceholdersToResolvedValues(string, variableManager.getVariableValuesByIds())

    private suspend fun executeShortcut() {
        val response = try {
            httpRequester
                .executeShortcut(
                    context,
                    shortcut,
                    variableManager,
                    ResponseFileStorage(context, shortcut.id),
                    fileUploadManager,
                    if (shortcut.acceptCookies) cookieJar else null,
                )
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

        when (shortcut.responseHandling?.successOutput) {
            ResponseHandlingModel.SUCCESS_OUTPUT_MESSAGE -> {
                displayOutput(
                    output = shortcut.responseHandling
                        ?.successMessage
                        ?.takeUnlessEmpty()
                        ?.let(::injectVariables)
                        ?: String.format(getString(R.string.executed), shortcutName),
                    response = response,
                )
            }
            ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE -> displayOutput(output = null, response)
            ResponseHandlingModel.SUCCESS_OUTPUT_NONE -> Unit
            else -> Unit
        }
    }

    private suspend fun rescheduleExecution() {
        if (tryNumber < MAX_RETRY) {
            val waitUntil = DateUtil.calculateDate(calculateDelay())
            pendingExecutionsRepository
                .createPendingExecution(
                    shortcutId = shortcut.id,
                    resolvedVariables = variableManager.getVariableValuesByKeys(),
                    tryNumber = tryNumber + 1,
                    waitUntil = waitUntil,
                    recursionDepth = recursionDepth,
                    requiresNetwork = shortcut.isWaitForNetwork,
                )
        }
    }

    private fun calculateDelay() =
        RETRY_BACKOFF.pow(tryNumber.toDouble()).toInt() * 1000

    private fun generateOutputFromError(error: Throwable, simple: Boolean = false) =
        ErrorFormatter(context).getPrettyError(error, shortcutName, includeBody = !simple)

    private fun showProgress() {
        if (shortcut.isFeedbackInDialog || shortcut.isFeedbackInWindow) {
            progressIndicator.showProgress()
        } else {
            progressIndicator.showProgressDelayed(INVISIBLE_PROGRESS_THRESHOLD)
        }
    }

    private suspend fun displayOutput(output: String?, response: ShortcutResponse? = null) {
        when (shortcut.responseHandling?.uiType) {
            ResponseHandlingModel.UI_TYPE_TOAST -> {
                showToast(
                    (output ?: response?.getContentAsString(context) ?: "")
                        .truncate(maxLength = TOAST_MAX_LENGTH)
                        .let(HTMLUtil::format)
                        .ifBlank { getString(R.string.message_blank_response) },
                    long = shortcut.responseHandling?.successOutput == ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE
                )
            }
            ResponseHandlingModel.UI_TYPE_DIALOG,
            null,
            -> {
                DialogBuilder(context)
                    .title(shortcutName)
                    .let { builder ->
                        if (output == null && isImage(response?.contentType)) {
                            val imageView = ImageView(context)
                            imageView.loadImage(response!!.contentFile!!, preventMemoryCache = true)
                            builder.view(imageView)
                        } else {
                            val view = DialogTextBinding.inflate(LayoutInflater.from(context))
                            val textView = view.text
                            val finalOutput = (output ?: response?.getContentAsString(context) ?: "")
                                .ifBlank { getString(R.string.message_blank_response) }
                                .let {
                                    HTMLUtil.formatWithImageSupport(it, context, textView::reloadImageSpans, lifecycleScope)
                                }
                            textView.text = finalOutput
                            textView.movementMethod = LinkMovementMethod.getInstance()
                            builder.view(textView)
                        }
                    }
                    .positive(R.string.dialog_ok)
                    .runIfNotNull(shortcut.responseHandling?.displayActions?.firstOrNull()) { action ->
                        val text = output ?: response?.getContentAsString(context) ?: ""
                        when (action) {
                            ResponseDisplayAction.RERUN -> {
                                neutral(R.string.action_rerun_shortcut) {
                                    rerunShortcut()
                                }
                            }
                            ResponseDisplayAction.SHARE -> {
                                runIf(text.isNotEmpty() && text.length < MAX_SHARE_LENGTH) {
                                    neutral(R.string.share_button) {
                                        shareResponse(text, response?.contentType ?: "", response?.contentFile)
                                    }
                                }
                            }
                            ResponseDisplayAction.COPY -> {
                                runIf(text.isNotEmpty() && text.length < MAX_COPY_LENGTH) {
                                    neutral(R.string.action_copy_response) {
                                        copyResponse(text)
                                    }
                                }
                            }
                            ResponseDisplayAction.SAVE -> this
                        }
                    }
                    .showAndAwaitDismissal()
            }
            ResponseHandlingModel.UI_TYPE_WINDOW -> {
                progressIndicator.hideProgress()
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
                    .startActivity(this)
            }
            else -> Unit
        }
    }

    private fun rerunShortcut() {
        IntentBuilder(shortcut.id)
            .startActivity(context)
        finishWithoutAnimation()
    }

    private fun shareResponse(text: String, type: String, responseFileUri: Uri?) {
        if (shouldShareAsText(text, type)) {
            ShareUtil.shareText(context, text)
        } else {
            Intent(Intent.ACTION_SEND)
                .setType(type)
                .putExtra(Intent.EXTRA_STREAM, responseFileUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .let {
                    Intent.createChooser(it, shortcutName)
                }
                .startActivity(this)
        }
    }

    private fun shouldShareAsText(text: String, type: String) =
        !isImage(type) && text.length < MAX_SHARE_LENGTH

    private fun copyResponse(text: String) {
        clipboardUtil.copyToClipboard(text)
    }

    private fun resumeAfterFileRequest(fileUris: List<Uri>?) {
        if (fileUploadManager == null) {
            // TODO: Handle edge case where variableManager is no longer set because activity was recreated
            logException(RuntimeException("Failed to resume after file sharing"))
            showToast(R.string.error_generic, long = true)
            finishWithoutAnimation()
            return
        }
        fileUploadManager!!.fulfilFileRequest(fileUris ?: emptyList())
        lifecycleScope.launch {
            subscribeAndFinishAfterIfNeeded {
                executeWithFileRequests()
            }
        }
    }

    override fun onBackPressed() {
        finishWithoutAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        SessionMonitor.onSessionComplete()
    }

    class IntentBuilder(private val shortcutId: ShortcutId? = null) : BaseIntentBuilder(ExecuteActivity::class) {

        init {
            intent.action = ACTION_EXECUTE_SHORTCUT
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
        }

        override fun build(context: Context): Intent {
            if (shortcutId != null) {
                shortcut(shortcutId, context)
            }
            return super.build(context)
        }

        private fun shortcut(shortcutId: ShortcutId, context: Context) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
            intent.data = Uri.fromParts("content", context.packageName, null)
                .buildUpon()
                .appendPath(shortcutId)
                .build()
        }

        fun tryNumber(tryNumber: Int) = also {
            if (tryNumber > 0) {
                intent.putExtra(EXTRA_TRY_NUMBER, tryNumber)
            }
        }

        fun variableValues(variableValues: Map<VariableKey, String>) = also {
            intent.putExtra(EXTRA_VARIABLE_VALUES, HashMap(variableValues))
        }

        fun recursionDepth(recursionDepth: Int) = also {
            intent.putExtra(EXTRA_RECURSION_DEPTH, recursionDepth)
        }

        fun files(files: List<Uri>) = also {
            intent.putParcelableArrayListExtra(
                EXTRA_FILES,
                ArrayList<Uri>().apply { addAll(files) }
            )
        }

        fun executionId(id: String) = also {
            intent.putExtra(EXTRA_EXECUTION_SCHEDULE_ID, id)
        }
    }

    companion object {

        const val ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute"

        const val EXTRA_SHORTCUT_ID = "id"
        const val EXTRA_VARIABLE_VALUES = "variable_values"
        const val EXTRA_TRY_NUMBER = "try_number"
        const val EXTRA_RECURSION_DEPTH = "recursion_depth"
        const val EXTRA_FILES = "files"
        const val EXTRA_EXECUTION_SCHEDULE_ID = "schedule_id"

        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val MAX_SHARE_LENGTH = 300000
        private const val MAX_COPY_LENGTH = 300000
        private const val TOAST_MAX_LENGTH = 400

        private const val INVISIBLE_PROGRESS_THRESHOLD = 1000L

        private fun isExpected(throwable: Throwable?) =
            throwable is ErrorResponse ||
                throwable is IOException ||
                throwable is UserException ||
                throwable is CanceledByUserException ||
                throwable is NoActivityAvailableException ||
                throwable is ResumeLaterException
    }
}
