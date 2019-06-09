package ch.rmy.android.http_shortcuts.activities

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.extensions.truncate
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables
import com.afollestad.materialdialogs.MaterialDialog
import com.android.volley.VolleyError
import com.github.chen0040.androidcodeview.SourceCodeView
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import java.util.*

class ExecuteActivity : BaseActivity() {

    private val controller by lazy {
        destroyer.own(Controller())
    }
    private lateinit var shortcut: Shortcut
    private var lastResponse: ShortcutResponse? = null

    private var progressDialog: ProgressDialog? = null

    private val responseText: TextView by bindView(R.id.response_text)
    private val responseTextContainer: View by bindView(R.id.response_text_container)
    private val formattedResponseText: SourceCodeView by bindView(R.id.formatted_response_text)
    private val progressSpinner: CircularProgressBar by bindView(R.id.progress_spinner)

    private val actionFactory by lazy {
        ActionFactory(context)
    }
    private val scriptExecutor: ScriptExecutor by lazy {
        ScriptExecutor(actionFactory)
    }

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
    private val shortcutName by lazy {
        shortcut.name.ifEmpty { getString(R.string.shortcut_safe_name) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shortcut = controller.getShortcutById(shortcutId)?.detachFromRealm() ?: run {
            showToast(getString(R.string.shortcut_not_found), long = true)
            finishWithoutAnimation()
            return
        }
        if (shortcut.isFeedbackUsesUI) {
            title = shortcutName
            destroyer.own(::hideProgress)
            if (shortcut.isFeedbackInWindow) {
                setContentView(R.layout.activity_execute)
            }
        }

        destroyer.own {
            ExecutionScheduler.schedule(context)
        }

        if (requiresConfirmation()) {
            promptForConfirmation()
        } else {
            Completable.complete()
        }
            .concatWith(resolveVariablesAndExecute(variableValues))
            .onErrorResumeNext { error ->
                ExecuteErrorHandler(context).handleError(error)
            }
            .subscribe(
                {
                    if (shouldFinishAfterExecution()) {
                        finishWithoutAnimation()
                    }
                },
                {
                    finishWithoutAnimation()
                }
            )
            .attachTo(destroyer)
    }

    private fun requiresConfirmation() =
        shortcut.requireConfirmation && tryNumber == 0

    private fun shouldFinishAfterExecution() =
        !shortcut.isFeedbackUsesUI || shouldDelayExecution()

    private fun shouldDelayExecution() =
        shortcut.delay > 0 && tryNumber == 0

    private fun promptForConfirmation(): Completable =
        Completable.create { emitter ->
            MaterialDialog.Builder(context)
                .title(shortcutName)
                .content(R.string.dialog_message_confirm_shortcut_execution)
                .dismissListener {
                    emitter.cancel()
                }
                .positiveText(R.string.dialog_ok)
                .onPositive { _, _ ->
                    emitter.onComplete()
                }
                .negativeText(R.string.dialog_cancel)
                .showIfPossible()
                ?: run {
                    emitter.cancel()
                }
        }

    private fun resolveVariablesAndExecute(variableValues: Map<String, String>): Completable =
        VariableResolver(context)
            .resolve(controller, shortcut, variableValues)
            .flatMapCompletable { resolvedVariables ->
                if (shouldDelayExecution()) {
                    val waitUntil = DateUtil.calculateDate(shortcut.delay)
                    controller.createPendingExecution(shortcut.id, resolvedVariables, tryNumber, waitUntil, shortcut.isWaitForNetwork)
                } else {
                    executeWithActions(resolvedVariables.toMutableMap())
                }
            }

    private fun executeWithActions(resolvedVariables: MutableMap<String, String>): Completable =
        Completable.fromAction {
            showProgress()
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .concatWith(
                if (tryNumber == 0 || (tryNumber == 1 && shortcut.delay > 0)) {
                    scriptExecutor.execute(context, shortcut.codeOnPrepare, shortcut.id, resolvedVariables)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                } else {
                    Completable.complete()
                }
            )
            .concatWith(
                if (shortcut.isBrowserShortcut) {
                    openShortcutInBrowser(resolvedVariables)
                    Completable.complete()
                } else {
                    executeShortcut(resolvedVariables)
                        .flatMapCompletable { response ->
                            scriptExecutor.execute(
                                context = context,
                                script = shortcut.codeOnSuccess,
                                shortcutId = shortcut.id,
                                variableValues = resolvedVariables,
                                response = response,
                                recursionDepth = recursionDepth
                            )
                                .subscribeOn(Schedulers.computation())
                                .observeOn(AndroidSchedulers.mainThread())
                        }
                        .onErrorResumeNext { error ->
                            scriptExecutor.execute(
                                context = context,
                                script = shortcut.codeOnFailure,
                                shortcutId = shortcut.id,
                                variableValues = resolvedVariables,
                                volleyError = error as? VolleyError?,
                                recursionDepth = recursionDepth
                            )
                                .subscribeOn(Schedulers.computation())
                                .observeOn(AndroidSchedulers.mainThread())
                        }
                }
            )
            .doOnTerminate {
                hideProgress()
            }

    private fun openShortcutInBrowser(resolvedVariables: MutableMap<String, String>) {
        val url = Variables.rawPlaceholdersToResolvedValues(shortcut.url, resolvedVariables)
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

    private fun executeShortcut(resolvedVariables: Map<String, String>): Single<ShortcutResponse> =
        HttpRequester.executeShortcut(context, shortcut, resolvedVariables)
            .doOnSuccess { response ->
                setLastResponse(response)
                if (shortcut.isFeedbackErrorsOnly()) {
                    finishWithoutAnimation()
                } else {
                    val simple = shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE
                    val output = if (simple) String.format(getString(R.string.executed), shortcutName) else generateOutputFromResponse(response)
                    displayOutput(output, response.contentType)
                }
            }
            .doOnError { error ->
                if (!shortcut.isFeedbackUsesUI && shortcut.isWaitForNetwork && (error as? VolleyError)?.networkResponse == null) {
                    rescheduleExecution(resolvedVariables)
                    if (shortcut.feedback != Shortcut.FEEDBACK_NONE && tryNumber == 0) {
                        showToast(String.format(context.getString(R.string.execution_delayed), shortcutName), long = true)
                    }
                    finishWithoutAnimation()
                } else {
                    setLastResponse(null)
                    val simple = shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE_ERRORS || shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE
                    displayOutput(generateOutputFromError(error, simple), ShortcutResponse.TYPE_TEXT)
                }
            }

    private fun rescheduleExecution(resolvedVariables: Map<String, String>) {
        if (tryNumber < MAX_RETRY) {
            val waitUntil = DateUtil.calculateDate(calculateDelay())
            controller.createPendingExecution(shortcut.id, resolvedVariables, tryNumber, waitUntil, shortcut.isWaitForNetwork)
                .subscribe {
                    ExecutionScheduler.schedule(context)
                }
                .attachTo(destroyer)
        }
    }

    private fun calculateDelay() = Math.pow(RETRY_BACKOFF, tryNumber.toDouble()).toInt() * 1000

    private fun generateOutputFromResponse(response: ShortcutResponse) = response.bodyAsString

    private fun generateOutputFromError(error: Throwable, simple: Boolean): String {
        if (error is VolleyError && error.networkResponse != null) {
            val builder = StringBuilder()
            builder.append(String.format(getString(R.string.error_http), shortcutName, error.networkResponse.statusCode))

            if (!simple && error.networkResponse.data != null) {
                try {
                    builder.append("\n\n")
                    builder.append(String(error.networkResponse.data))
                } catch (e: Exception) {
                    logException(e)
                }
            }

            return builder.toString()
        } else {
            return when {
                error.cause?.message != null -> String.format(getString(R.string.error_other), shortcutName, error.cause!!.message)
                error.message != null -> String.format(getString(R.string.error_other), shortcutName, error.message)
                else -> String.format(getString(R.string.error_other), shortcutName, error.javaClass.simpleName)
            }
        }
    }

    private fun showProgress() {
        when {
            shortcut.isFeedbackInDialog -> {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(context, null, String.format(getString(R.string.progress_dialog_message), shortcutName))
                }
            }
            shortcut.isFeedbackInWindow -> {
                progressSpinner.visible = true
                responseTextContainer.visible = false
                formattedResponseText.visible = false
            }
        }
    }

    private fun hideProgress() {
        when {
            shortcut.isFeedbackInDialog -> {
                progressDialog?.dismiss()
                progressDialog = null
            }
            shortcut.isFeedbackInWindow -> {
                progressSpinner.visible = false
            }
        }
    }

    private fun displayOutput(output: String, type: String) {
        when (shortcut.feedback) {
            Shortcut.FEEDBACK_TOAST_SIMPLE, Shortcut.FEEDBACK_TOAST_SIMPLE_ERRORS -> {
                showToast(output)
            }
            Shortcut.FEEDBACK_TOAST, Shortcut.FEEDBACK_TOAST_ERRORS -> {
                showToast(output.truncate(maxLength = TOAST_MAX_LENGTH), long = true)
            }
            Shortcut.FEEDBACK_DIALOG -> {
                MaterialDialog.Builder(context)
                    .title(shortcutName)
                    .content(output)
                    .positiveText(R.string.dialog_ok)
                    .dismissListener { finishWithoutAnimation() }
                    .show()
            }
            Shortcut.FEEDBACK_ACTIVITY -> {
                when (type) {
                    ShortcutResponse.TYPE_JSON -> {
                        formattedResponseText.setCode(GsonUtil.prettyPrint(output), "json")
                        formattedResponseText.visible = true
                    }
                    ShortcutResponse.TYPE_XML -> {
                        formattedResponseText.setCode(output, "xml")
                        formattedResponseText.visible = true
                    }
                    else -> {
                        responseText.text = output
                        responseTextContainer.visible = true
                    }
                }
            }
        }
    }

    private fun setLastResponse(response: ShortcutResponse?) {
        this.lastResponse = response
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.execute_activity_menu, menu)
        menu.findItem(R.id.action_share_response).isVisible = canShareResponse()
        return super.onCreateOptionsMenu(menu)
    }

    private fun canShareResponse() =
        lastResponse != null && lastResponse!!.bodyAsString.length < MAX_SHARE_LENGTH

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_share_response -> consume { shareLastResponse() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun shareLastResponse() {
        if (!canShareResponse()) {
            return
        }
        try {
            Intent(Intent.ACTION_SEND)
                .setType(ShortcutResponse.TYPE_TEXT)
                .putExtra(Intent.EXTRA_TEXT, lastResponse!!.bodyAsString)
                .let {
                    Intent.createChooser(it, getString(R.string.share_title))
                        .startActivity(this)
                }
        } catch (e: Exception) {
            showToast(getString(R.string.error_share_failed), long = true)
            logException(e)
        }
    }

    override val navigateUpIcon = R.drawable.ic_clear

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

    }

    companion object {

        const val ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.resolveVariablesAndExecute"
        const val EXTRA_SHORTCUT_ID = "id"
        const val EXTRA_VARIABLE_VALUES = "variable_values"
        const val EXTRA_TRY_NUMBER = "try_number"
        const val EXTRA_RECURSION_DEPTH = "recursion_depth"

        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400

        private const val MAX_SHARE_LENGTH = 500000

    }

}
