package ch.rmy.android.http_shortcuts.activities

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ProgressIndicator
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.exceptions.MissingLocationPermissionException
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.exceptions.ResumeLaterException
import ch.rmy.android.http_shortcuts.exceptions.UnsupportedFeatureException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.finishWithoutAnimation
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.extensions.truncate
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.ResponseFileStorage
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.scripting.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.CookieJar
import java.io.IOException
import java.net.UnknownHostException
import java.util.HashMap
import kotlin.math.pow

class ExecuteActivity : BaseActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    private val controller by lazy {
        destroyer.own(Controller())
    }
    private lateinit var base: Base
    private lateinit var shortcut: Shortcut

    private val progressIndicator: ProgressIndicator by lazy {
        ProgressIndicator(this)
    }

    private val scriptExecutor: ScriptExecutor by lazy {
        ScriptExecutor(context, ActionFactory())
    }

    /* Execution Parameters */
    private val shortcutId: String by lazy {
        IntentUtil.getShortcutId(intent)
    }
    private val variableValues by lazy {
        IntentUtil.getVariableValues(intent)
    }
    private val tryNumber by lazy {
        intent.extras?.getInt(EXTRA_TRY_NUMBER) ?: 0
    }
    private val recursionDepth by lazy {
        intent?.extras?.getInt(EXTRA_RECURSION_DEPTH) ?: 0
    }
    private val fileUris: List<Uri> by lazy {
        intent?.extras?.getParcelableArrayList<Uri>(EXTRA_FILES) ?: emptyList<Uri>()
    }
    private val shortcutName by lazy {
        shortcut.name.ifEmpty { getString(R.string.shortcut_safe_name) }
    }
    private val globalCode: String by lazy {
        base.globalCode ?: ""
    }

    /* Caches / State */
    private var fileUploadManager: FileUploadManager? = null
    private lateinit var variableManager: VariableManager
    private val cookieJar: CookieJar by lazy {
        CookieManager.getCookieJar(context)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Commons
            .createPendingExecution(
                shortcutId = IntentUtil.getShortcutId(intent),
                resolvedVariables = IntentUtil.getVariableValues(intent),
                tryNumber = 0
            )
            .subscribe()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isRealmAvailable) {
            return
        }

        shortcut = controller.getShortcutById(shortcutId)?.detachFromRealm() ?: run {
            showToast(getString(R.string.shortcut_not_found), long = true)
            finishWithoutAnimation()
            return
        }
        base = controller.getBase().detachFromRealm()
        setTheme(themeHelper.transparentTheme)

        destroyer.own {
            if (fileUploadManager != null) {
                FileUtil.deleteOldCacheFiles(context)
            }
            ExecutionScheduler.schedule(context)
        }

        subscribeAndFinishAfterIfNeeded(
            promptForConfirmationIfNeeded()
                .concatWith(resolveVariablesAndExecute())
        )
    }

    private fun subscribeAndFinishAfterIfNeeded(completable: Completable) {
        completable
            .doOnError { error ->
                if (!isExpected(error)) {
                    logException(error)
                }
            }
            .onErrorResumeNext { error ->
                when (error) {
                    is ResumeLaterException -> {
                        Completable.error(error)
                    }
                    is CanceledByUserException -> {
                        Completable.complete()
                    }
                    is UserException -> {
                        displayError(error)
                    }
                    else -> {
                        if (shouldReschedule(error)) {
                            if (shortcut.responseHandling?.successOutput != ResponseHandling.SUCCESS_OUTPUT_NONE && tryNumber == 0) {
                                showToast(String.format(context.getString(R.string.execution_delayed), shortcutName), long = true)
                            }
                            rescheduleExecution()
                                .doOnComplete {
                                    ExecutionScheduler.schedule(context)
                                }
                        } else {
                            val simple = shortcut.responseHandling?.failureOutput == ResponseHandling.FAILURE_OUTPUT_SIMPLE
                            displayOutput(
                                generateOutputFromError(error, simple),
                                response = (error as? ErrorResponse)?.shortcutResponse
                            )
                        }
                    }
                }
            }
            .subscribe(
                {
                    finishWithoutAnimation()
                },
                { error ->
                    if (error !is ResumeLaterException) {
                        logException(error)
                        finishWithoutAnimation()
                    }
                }
            )
    }

    private fun requiresConfirmation() =
        shortcut.requireConfirmation && tryNumber == 0

    private fun shouldFinishAfterExecution() =
        !shortcut.isFeedbackUsingUI || shouldDelayExecution()

    private fun shouldDelayExecution() =
        shortcut.delay > 0 && tryNumber == 0

    private fun shouldReschedule(error: Throwable): Boolean =
        shortcut.isWaitForNetwork
            && error !is ErrorResponse
            && (error is UnknownHostException || !NetworkUtil.isNetworkConnected(context))

    private fun shouldFinishImmediately() =
        shouldFinishAfterExecution()
            && shortcut.codeOnPrepare.isEmpty()
            && shortcut.codeOnSuccess.isEmpty()
            && shortcut.codeOnFailure.isEmpty()
            && !NetworkUtil.isNetworkPerformanceRestricted(context)
            && !Settings(context).isForceForegroundEnabled

    private fun promptForConfirmationIfNeeded(): Completable =
        if (requiresConfirmation()) {
            promptForConfirmation()
        } else {
            Completable.complete()
        }

    private fun promptForConfirmation(): Completable =
        Completable.create { emitter ->
            DialogBuilder(context)
                .title(shortcutName)
                .message(R.string.dialog_message_confirm_shortcut_execution)
                .dismissListener {
                    emitter.cancel()
                }
                .positive(R.string.dialog_ok) {
                    emitter.onComplete()
                }
                .negative(R.string.dialog_cancel)
                .showIfPossible()
                ?: run {
                    emitter.cancel()
                }
        }

    private fun displayError(error: Throwable): Completable =
        generateOutputFromError(error)
            .let { message ->
                if (isFinishing) {
                    showToast(message, long = true)
                    Completable.complete()
                } else {
                    DialogBuilder(context)
                        .title(R.string.dialog_title_error)
                        .message(message)
                        .positive(R.string.dialog_ok)
                        .showAsCompletable()
                }
            }

    private fun resolveVariablesAndExecute(): Completable =
        VariableResolver(context)
            .resolve(
                controller.getVariables().detachFromRealm(),
                shortcut,
                globalCode,
                variableValues,
            )
            .flatMapCompletable { variableManager ->
                this.variableManager = variableManager
                if (shouldDelayExecution()) {
                    val waitUntil = DateUtil.calculateDate(shortcut.delay)
                    Commons.createPendingExecution(
                        shortcutId = shortcut.id,
                        resolvedVariables = variableManager.getVariableValuesByKeys(),
                        waitUntil = waitUntil,
                        tryNumber = 1,
                        recursionDepth = recursionDepth,
                        requiresNetwork = shortcut.isWaitForNetwork
                    )
                } else {
                    executeWithFileRequests()
                }
            }

    private fun createFileUploadManagerIfNeeded() {
        if (fileUploadManager != null || (!shortcut.usesRequestParameters() && !shortcut.usesFileBody())) {
            return
        }
        fileUploadManager = FileUploadManager.Builder(contentResolver)
            .withSharedFiles(fileUris)
            .mapIf(shortcut.usesFileBody()) {
                it.addFileRequest()
            }
            .mapFor(shortcut.parameters) { builder, parameter ->
                when {
                    parameter.isFilesParameter -> {
                        builder.addFileRequest(multiple = true)
                    }
                    parameter.isFileParameter -> {
                        builder.addFileRequest(multiple = false)
                    }
                    else -> builder
                }
            }
            .build()
    }

    private fun executeWithFileRequests(): Completable {
        createFileUploadManagerIfNeeded()
        val fileRequest = fileUploadManager?.getNextFileRequest()
        return if (fileRequest == null) {
            executeWithActions()
        } else {
            openFilePickerForFileParameter(multiple = fileRequest.multiple)
        }
    }

    private fun openFilePickerForFileParameter(multiple: Boolean): Completable =
        try {
            FilePickerUtil.createIntent(multiple)
                .startActivity(this, REQUEST_PICK_FILES)
            Completable.error(ResumeLaterException())
        } catch (e: ActivityNotFoundException) {
            Completable.error(UnsupportedFeatureException())
        }

    private fun checkWifiNetworkSsid(): Completable =
        if (shortcut.wifiSsid.isEmpty()) {
            Completable.fromAction {
                finishActivityIfNeeded()
            }
        } else {
            showWifiDialogIfNeeded()
        }

    private fun showWifiDialogIfNeeded(): Completable = Completable.create { emitter ->
        if (getCurrentSsid() == shortcut.wifiSsid) {
            finishActivityIfNeeded()
            emitter.onComplete()
        } else {
            showWifiPickerConfirmation(emitter)
        }
    }

    private fun getCurrentSsid(): String =
        (applicationContext.getSystemService(WIFI_SERVICE) as WifiManager)
            .connectionInfo
            .ssid
            .trim('"')

    private fun showWifiPickerConfirmation(emitter: CompletableEmitter) {
        DialogBuilder(context)
            .title(shortcutName)
            .message(getString(R.string.message_wrong_wifi_network, shortcut.wifiSsid))
            .dismissListener {
                emitter.cancel()
            }
            .positive(getString(R.string.action_label_select)) {
                showWifiPicker()
                emitter.cancel()
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
            ?: run {
                emitter.cancel()
            }
    }

    private fun showWifiPicker() {
        Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)
            .putExtra("extra_prefs_show_button_bar", true)
            .putExtra("wifi_enable_next_on_connect", true)
            .startActivity(this)
    }

    private fun requestPermissionsForWifiCheckIfNeeded(): Completable =
        if (shortcut.wifiSsid.isEmpty()) {
            Completable.complete()
        } else {
            showRequestPermissionRationalIfNeeded().concatWith(requestPermissionsForWifiCheck())
        }

    private fun finishActivityIfNeeded() {
        if (shouldFinishImmediately()) {
            finishWithoutAnimation()
        }
        showProgress()
    }

    private fun showRequestPermissionRationalIfNeeded(): Completable =
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            DialogBuilder(context)
                .title(getString(R.string.title_permission_dialog))
                .message(getString(R.string.message_permission_rational))
                .positive(R.string.dialog_ok)
                .showAsCompletable()
        } else {
            Completable.complete()
        }

    private fun requestPermissionsForWifiCheck() = Completable.create { emitter ->
        RxPermissions(this)
            .request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe { granted ->
                if (granted) {
                    emitter.onComplete()
                } else {
                    emitter.onError(MissingLocationPermissionException())
                }
            }
    }

    private fun executeWithActions(): Completable =
        requestPermissionsForWifiCheckIfNeeded()
            .subscribeOn(AndroidSchedulers.mainThread())
            .concatWith(checkWifiNetworkSsid())
            .concatWith(
                if (tryNumber == 0 || (tryNumber == 1 && shortcut.delay > 0)) {
                    val script = if (globalCode.isEmpty()) {
                        shortcut.codeOnPrepare
                    } else {
                        "$globalCode;\n${shortcut.codeOnPrepare}"
                    }
                    scriptExecutor.execute(
                        script = script,
                        shortcut = shortcut,
                        variableManager = variableManager,
                        fileUploadManager = fileUploadManager,
                        recursionDepth = recursionDepth,
                    )
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                } else {
                    Completable.complete()
                }
            )
            .concatWith(
                when (shortcut.type) {
                    ShortcutExecutionType.APP -> executeShortcut()
                    ShortcutExecutionType.BROWSER -> openShortcutInBrowser()
                    else -> Completable.complete()
                }
            )

    private fun openShortcutInBrowser(): Completable = Completable.fromAction {
        val url = injectVariables(shortcut.url)
        try {
            val uri = Uri.parse(url)
            if (!Validation.isValidUrl(uri)) {
                throw InvalidUrlException(url)
            }
            Intent(Intent.ACTION_VIEW, uri)
                .startActivity(this)
        } catch (e: ActivityNotFoundException) {
            throw UnsupportedFeatureException()
        }
    }

    private fun injectVariables(string: String): String =
        Variables.rawPlaceholdersToResolvedValues(string, variableManager.getVariableValuesByIds())

    private fun executeShortcut(): Completable =
        HttpRequester(contentResolver)
            .executeShortcut(
                shortcut,
                variableManager,
                ResponseFileStorage(context, shortcutId),
                fileUploadManager,
                if (shortcut.acceptCookies) cookieJar else null,
            )
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext { error ->
                if (error is ErrorResponse || error is IOException) {
                    scriptExecutor
                        .execute(
                            script = shortcut.codeOnFailure,
                            shortcut = shortcut,
                            variableManager = variableManager,
                            fileUploadManager = fileUploadManager,
                            error = error as? Exception,
                            recursionDepth = recursionDepth
                        )
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .andThen(Single.error(error))
                } else {
                    Single.error(error)
                }
            }
            .flatMap { response ->
                scriptExecutor
                    .execute(
                        script = shortcut.codeOnSuccess,
                        shortcut = shortcut,
                        variableManager = variableManager,
                        fileUploadManager = fileUploadManager,
                        response = response,
                        recursionDepth = recursionDepth
                    )
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toSingle { response }
            }
            .flatMapCompletable { response ->
                val outputType = shortcut.responseHandling?.successOutput
                if (outputType == ResponseHandling.SUCCESS_OUTPUT_NONE) {
                    Completable.complete()
                } else {
                    val output = if (outputType == ResponseHandling.SUCCESS_OUTPUT_MESSAGE) {
                        shortcut.responseHandling
                            ?.successMessage
                            ?.takeUnlessEmpty()
                            ?.let(::injectVariables)
                            ?: String.format(getString(R.string.executed), shortcutName)
                    } else {
                        generateOutputFromResponse(response)
                    }
                    displayOutput(output, response)
                }
            }

    private fun rescheduleExecution(): Completable =
        if (tryNumber < MAX_RETRY) {
            val waitUntil = DateUtil.calculateDate(calculateDelay())
            Commons
                .createPendingExecution(
                    shortcutId = shortcut.id,
                    resolvedVariables = variableManager.getVariableValuesByKeys(),
                    tryNumber = tryNumber + 1,
                    waitUntil = waitUntil,
                    recursionDepth = recursionDepth,
                    requiresNetwork = shortcut.isWaitForNetwork
                )
        } else {
            Completable.complete()
        }

    private fun calculateDelay() = RETRY_BACKOFF.pow(tryNumber.toDouble()).toInt() * 1000

    private fun generateOutputFromResponse(response: ShortcutResponse) =
        try {
            response.getContentAsString(context)
        } catch (e: ResponseTooLargeException) {
            e.getLocalizedMessage(context)
        }

    private fun generateOutputFromError(error: Throwable, simple: Boolean = false) =
        ErrorFormatter(context).getPrettyError(error, shortcutName, includeBody = !simple)

    private fun showProgress() {
        if (shortcut.isFeedbackInDialog || shortcut.isFeedbackInWindow) {
            progressIndicator.showProgress()
        } else {
            progressIndicator.showProgressDelayed(INVISIBLE_PROGRESS_THRESHOLD)
        }
    }

    private fun displayOutput(output: String, response: ShortcutResponse? = null): Completable =
        when (shortcut.responseHandling?.uiType) {
            ResponseHandling.UI_TYPE_TOAST -> {
                showToast(
                    output
                        .truncate(maxLength = TOAST_MAX_LENGTH)
                        .ifBlank { getString(R.string.message_blank_response) },
                    long = shortcut.responseHandling?.successOutput == ResponseHandling.SUCCESS_OUTPUT_RESPONSE
                )
                Completable.complete()
            }
            ResponseHandling.UI_TYPE_DIALOG -> {
                DialogBuilder(context)
                    .title(shortcutName)
                    .message(HTMLUtil.format(output.ifBlank { getString(R.string.message_blank_response) }))
                    .positive(R.string.dialog_ok)
                    .showAsCompletable()
            }
            ResponseHandling.UI_TYPE_WINDOW -> {
                DisplayResponseActivity.IntentBuilder(context, shortcutId)
                    .name(shortcutName)
                    .type(response?.contentType)
                    .text(output)
                    .responseFileUri(response?.contentFile)
                    .url(response?.url)
                    .mapIf(shortcut.responseHandling?.includeMetaInfo == true) {
                        it.showDetails(true)
                            .timing(response?.timing)
                            .headers(response?.headers)
                            .statusCode(response?.statusCode)
                    }
                    .startActivity(this)
                Completable.complete()
            }
            else -> Completable.complete()
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_PICK_FILES -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    finishWithoutAnimation()
                    return
                }
                resumeAfterFileRequest(fileUris = FilePickerUtil.extractUris(data))
            }
        }
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
        subscribeAndFinishAfterIfNeeded(executeWithFileRequests())
    }

    override fun onBackPressed() {
        // Prevent cancelling. Not optimal, but will have to do for now
    }

    class IntentBuilder(context: Context, shortcutId: String? = null) : BaseIntentBuilder(context, ExecuteActivity::class.java) {

        init {
            intent.action = ACTION_EXECUTE_SHORTCUT
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            if (shortcutId != null) {
                shortcut(shortcutId, context)
            }
        }

        private fun shortcut(shortcutId: String, context: Context) = also {
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

        fun variableValues(variableValues: Map<String, String>) = also {
            intent.putExtra(EXTRA_VARIABLE_VALUES, HashMap(variableValues))
        }

        fun recursionDepth(recursionDepth: Int) = also {
            intent.putExtra(EXTRA_RECURSION_DEPTH, recursionDepth)
        }

        fun files(files: List<Uri>) = also {
            intent.putParcelableArrayListExtra(EXTRA_FILES, ArrayList<Uri>().apply { addAll(files) })
        }

    }

    companion object {

        const val ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.resolveVariablesAndExecute"

        const val EXTRA_SHORTCUT_ID = "id"
        const val EXTRA_VARIABLE_VALUES = "variable_values"
        const val EXTRA_TRY_NUMBER = "try_number"
        const val EXTRA_RECURSION_DEPTH = "recursion_depth"
        const val EXTRA_FILES = "files"

        private const val REQUEST_PICK_FILES = 1

        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400

        private const val INVISIBLE_PROGRESS_THRESHOLD = 1000L

        private fun isExpected(throwable: Throwable?) =
            throwable is ErrorResponse
                || throwable is IOException
                || throwable is UserException
                || throwable is CanceledByUserException
                || throwable is ResumeLaterException

    }

}
