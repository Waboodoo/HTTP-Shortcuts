package ch.rmy.android.http_shortcuts.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.exceptions.ResumeLaterException
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.finishWithoutAnimation
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.extensions.truncate
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.liquidplayer.javascript.JSException
import java.net.UnknownHostException
import java.util.HashMap
import kotlin.math.pow

class ExecuteActivity : BaseActivity() {

    override val initializeWithTheme: Boolean
        get() = false

    private val controller by lazy {
        destroyer.own(Controller())
    }
    private lateinit var shortcut: Shortcut

    private var layoutLoaded = false
    private val showProgressRunnable = Runnable {
        if (!layoutLoaded) {
            layoutLoaded = true
            setContentView(R.layout.activity_execute_loading)
            baseView?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private val handler = Handler()

    private val actionFactory by lazy {
        ActionFactory(context)
    }
    private val scriptExecutor: ScriptExecutor by lazy {
        ScriptExecutor(actionFactory)
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

    /* Caches / State */
    private var fileUploadManager: FileUploadManager? = null
    private var variableManager: VariableManager? = null

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
        setTheme(themeHelper.transparentTheme)

        destroyer.own {
            ExecutionScheduler.schedule(context)
        }

        subscribeAndFinishAfterIfNeeded(
            if (requiresConfirmation()) {
                promptForConfirmation()
            } else {
                Completable.complete()
            }
                .concatWith(resolveVariablesAndExecute(variableValues))
        )
    }

    private fun subscribeAndFinishAfterIfNeeded(completable: Completable) {
        completable
            .onErrorResumeNext { error ->
                if (error is ResumeLaterException) {
                    Completable.error(error)
                } else {
                    ExecuteErrorHandler(context).handleError(error)
                }
            }
            .subscribe(
                {
                    if (shouldFinishAfterExecution()) {
                        finishWithoutAnimation()
                    }
                },
                { error ->
                    if (error !is ResumeLaterException) {
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

    private fun shouldFinishImmediately() =
        shouldFinishAfterExecution()
            && shortcut.codeOnPrepare.isEmpty()
            && shortcut.codeOnSuccess.isEmpty()
            && shortcut.codeOnFailure.isEmpty()
            && !NetworkUtil.isNetworkPerformanceRestricted(context)

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

    private fun resolveVariablesAndExecute(variableValues: Map<String, String>): Completable =
        VariableResolver(context)
            .resolve(controller.getVariables().detachFromRealm(), shortcut, variableValues)
            .flatMapCompletable { variableManager ->
                if (shouldDelayExecution()) {
                    val waitUntil = DateUtil.calculateDate(shortcut.delay)
                    Commons.createPendingExecution(
                        shortcutId = shortcut.id,
                        resolvedVariables = variableManager.getVariableValuesByKeys(),
                        tryNumber = tryNumber,
                        waitUntil = waitUntil,
                        requiresNetwork = shortcut.isWaitForNetwork
                    )
                } else {
                    executeWithFileRequests(variableManager)
                }
            }

    private fun getFileUploadManager(): FileUploadManager? {
        if (!shortcut.usesRequestParameters()) {
            return null
        }
        if (fileUploadManager == null) {
            fileUploadManager = FileUploadManager.Builder(contentResolver)
                .withSharedFiles(fileUris)
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
        return fileUploadManager
    }

    private fun executeWithFileRequests(variableManager: VariableManager): Completable {
        val fileUploadManager = getFileUploadManager()
        val fileRequest = fileUploadManager?.getNextFileRequest()
        return if (fileRequest == null) {
            executeWithActions(variableManager, fileUploadManager)
        } else {
            this.variableManager = variableManager
            openFilePickerForFileParameter(multiple = fileRequest.multiple)
            Completable.error(ResumeLaterException())
        }
    }

    private fun openFilePickerForFileParameter(multiple: Boolean) {
        try {
            FilePickerUtil.createIntent(multiple)
                .startActivity(this, REQUEST_PICK_FILES)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
            finishWithoutAnimation()
        }
    }

    private fun executeWithActions(variableManager: VariableManager, fileUploadManager: FileUploadManager? = null): Completable =
        Completable
            .fromAction {
                if (shouldFinishImmediately()) {
                    finishWithoutAnimation()
                }
                showProgress()
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .concatWith(
                if (tryNumber == 0 || (tryNumber == 1 && shortcut.delay > 0)) {
                    scriptExecutor.execute(context, shortcut.codeOnPrepare, shortcut, variableManager)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                } else {
                    Completable.complete()
                }
            )
            .concatWith(
                if (shortcut.isBrowserShortcut) {
                    openShortcutInBrowser(variableManager)
                    Completable.complete()
                } else {
                    executeShortcut(variableManager, fileUploadManager)
                        .flatMapCompletable { response ->
                            scriptExecutor
                                .execute(
                                    context = context,
                                    script = shortcut.codeOnSuccess,
                                    shortcut = shortcut,
                                    variableManager = variableManager,
                                    response = response,
                                    recursionDepth = recursionDepth
                                )
                                .subscribeOn(Schedulers.computation())
                                .observeOn(AndroidSchedulers.mainThread())
                        }
                        .onErrorResumeNext { error ->
                            if (error is JSException) {
                                // TODO: Find a better way to differenciate network exceptions from JS exceptions
                                Completable.error(error)
                            } else {
                                scriptExecutor
                                    .execute(
                                        context = context,
                                        script = shortcut.codeOnFailure,
                                        shortcut = shortcut,
                                        variableManager = variableManager,
                                        error = error as? Exception,
                                        recursionDepth = recursionDepth
                                    )
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                            }
                        }
                }
            )

    private fun openShortcutInBrowser(variableManager: VariableManager) {
        val url = Variables.rawPlaceholdersToResolvedValues(shortcut.url, variableManager.getVariableValuesByIds())
        try {
            val uri = Uri.parse(url)
            if (!Validation.isValidUrl(uri)) {
                showToast(R.string.error_invalid_url)
                return
            }
            Intent(Intent.ACTION_VIEW, uri)
                .startActivity(this)
        } catch (e: ActivityNotFoundException) {
            showToast(R.string.error_not_supported)
        } catch (e: Exception) {
            logException(e)
            showToast(R.string.error_generic)
        }
    }

    private fun executeShortcut(variableManager: VariableManager, fileUploadManager: FileUploadManager? = null): Single<ShortcutResponse> =
        HttpRequester.executeShortcut(shortcut, variableManager, fileUploadManager)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { response ->
                if (shortcut.isFeedbackErrorsOnly()) {
                    finishWithoutAnimation()
                } else {
                    val simple = shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE
                    val output = if (simple) String.format(getString(R.string.executed), shortcutName) else generateOutputFromResponse(response)
                    displayOutput(output, response)
                }
            }
            .doOnError { error ->
                if (shortcut.isWaitForNetwork && error !is ErrorResponse && (error is UnknownHostException || !NetworkUtil.isNetworkConnected(context))) {
                    rescheduleExecution(variableManager)
                    if (shortcut.feedback != Shortcut.FEEDBACK_NONE && tryNumber == 0) {
                        showToast(String.format(context.getString(R.string.execution_delayed), shortcutName), long = true)
                    }
                    finishWithoutAnimation()
                } else {
                    logException(error)
                    val simple = shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE_ERRORS || shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE
                    displayOutput(generateOutputFromError(error, simple), response = (error as? ErrorResponse)?.shortcutResponse)
                }
            }

    private fun rescheduleExecution(variableManager: VariableManager) {
        if (tryNumber < MAX_RETRY) {
            val waitUntil = DateUtil.calculateDate(calculateDelay())
            Commons
                .createPendingExecution(
                    shortcut.id,
                    variableManager.getVariableValuesByKeys(),
                    tryNumber,
                    waitUntil,
                    shortcut.isWaitForNetwork
                )
                .subscribe {
                    ExecutionScheduler.schedule(context)
                }
                .attachTo(destroyer)
        }
    }

    private fun calculateDelay() = RETRY_BACKOFF.pow(tryNumber.toDouble()).toInt() * 1000

    private fun generateOutputFromResponse(response: ShortcutResponse) = response.bodyAsString

    private fun generateOutputFromError(error: Throwable, simple: Boolean) =
        ErrorFormatter(context).getPrettyError(error, shortcutName, includeBody = !simple)

    private fun showProgress() {
        when {
            shortcut.isFeedbackInDialog || shortcut.isFeedbackInWindow -> {
                handler.post(showProgressRunnable)
            }
            else -> {
                handler.removeCallbacks(showProgressRunnable)
                handler.postDelayed(showProgressRunnable, INVISIBLE_PROGRESS_THRESHOLD)
            }
        }
    }

    private fun displayOutput(output: String, response: ShortcutResponse? = null) {
        when (shortcut.feedback) {
            Shortcut.FEEDBACK_TOAST_SIMPLE, Shortcut.FEEDBACK_TOAST_SIMPLE_ERRORS -> {
                showToast(output.ifBlank { getString(R.string.message_blank_response) })
            }
            Shortcut.FEEDBACK_TOAST, Shortcut.FEEDBACK_TOAST_ERRORS -> {
                showToast(
                    output
                        .truncate(maxLength = TOAST_MAX_LENGTH)
                        .ifBlank { getString(R.string.message_blank_response) },
                    long = true
                )
            }
            Shortcut.FEEDBACK_DIALOG -> {
                DialogBuilder(context)
                    .title(shortcutName)
                    .message(HTMLUtil.format(output.ifBlank { getString(R.string.message_blank_response) }))
                    .positive(R.string.dialog_ok)
                    .dismissListener { finishWithoutAnimation() }
                    .show()
            }
            Shortcut.FEEDBACK_ACTIVITY, Shortcut.FEEDBACK_DEBUG -> {
                DisplayResponseActivity.IntentBuilder(context, shortcutId)
                    .name(shortcutName)
                    .type(response?.contentType)
                    .text(output)
                    .url(response?.url)
                    .mapIf(shortcut.feedback == Shortcut.FEEDBACK_DEBUG) {
                        it.showDetails(true)
                            .timing(response?.timing)
                            .headers(response?.headers)
                            .statusCode(response?.statusCode)
                    }
                    .build()
                    .startActivity(this)
                finishWithoutAnimation()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_PICK_FILES -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    finishWithoutAnimation()
                    return
                }
                val fileUris = FilePickerUtil.extractUris(data)
                getFileUploadManager()?.fulfilFileRequest(fileUris ?: emptyList())
                resumeAfterFileRequest()
            }
        }
    }

    private fun resumeAfterFileRequest() {
        if (variableManager == null || fileUploadManager == null) {
            // TODO: Handle edge case where variableManager is no longer set because activity was recreated
            logException(RuntimeException("Failed to resume after file sharing: variableManager=${variableManager != null}, fileUploadManger=${fileUploadManager != null}"))
            showToast(R.string.error_generic, long = true)
            finishWithoutAnimation()
            return
        }
        subscribeAndFinishAfterIfNeeded(executeWithFileRequests(variableManager!!))
    }

    override fun onBackPressed() {
        // Prevent cancelling. Not optimal, but will have to do for now
    }

    class IntentBuilder(context: Context, shortcutId: String) : BaseIntentBuilder(context, ExecuteActivity::class.java) {

        init {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
            intent.action = ACTION_EXECUTE_SHORTCUT
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
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

    }

}
