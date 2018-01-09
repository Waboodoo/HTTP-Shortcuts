package ch.rmy.android.http_shortcuts.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.http.ExecutionService
import ch.rmy.android.http_shortcuts.http.HttpRequester
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.consume
import ch.rmy.android.http_shortcuts.utils.visible
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import com.afollestad.materialdialogs.MaterialDialog
import com.android.volley.VolleyError
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import io.github.kbiakov.codeview.CodeView
import kotterknife.bindView
import org.jdeferred.Promise

class ExecuteActivity : BaseActivity() {

    private val controller = Controller()
    private lateinit var shortcut: Shortcut
    private var lastResponse: ShortcutResponse? = null

    private var progressDialog: ProgressDialog? = null

    private val responseText: TextView by bindView(R.id.response_text)
    private val responseTextContainer: View by bindView(R.id.response_text_container)
    private val formattedResponseText: CodeView by bindView(R.id.formatted_response_text)
    private val progressSpinner: CircularProgressBar by bindView(R.id.progress_spinner)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutId = IntentUtil.getShortcutId(intent)
        val variableValues = IntentUtil.getVariableValues(intent)
        val tryNumber = intent.extras?.getInt(EXTRA_TRY_NUMBER) ?: 0

        val shortcut = controller.getDetachedShortcutById(shortcutId)

        if (shortcut == null) {
            showToast(getString(R.string.shortcut_not_found), Toast.LENGTH_LONG)
            controller.destroy()
            finishWithoutAnimation()
            return
        }
        this.shortcut = shortcut
        title = shortcut.getSafeName(context)

        if (shortcut.feedback == Shortcut.FEEDBACK_ACTIVITY) {
            setTheme(R.style.LightTheme)
            setContentView(R.layout.activity_execute)
        }

        val promise = resolveVariablesAndExecute(variableValues, tryNumber)

        if (promise.isPending) {
            promise.done {
                if (!shortcut.feedbackUsesUI()) {
                    finishWithoutAnimation()
                }
            }.fail {
                finishWithoutAnimation()
            }
        } else {
            if (!shortcut.feedbackUsesUI()) {
                finishWithoutAnimation()
            }
        }
    }

    private fun resolveVariablesAndExecute(variableValues: Map<String, String>, tryNumber: Int): Promise<ResolvedVariables, Void, Void> {
        val variables = controller.variables
        return VariableResolver(context)
                .resolve(shortcut, variables, variableValues)
                .done { resolvedVariables ->
                    if (tryNumber == 0 && shortcut.delay > 0) {
                        val waitUntil = DateUtil.calculateDate(shortcut.delay)
                        controller.createPendingExecution(shortcut.id, resolvedVariables.toList(), tryNumber, waitUntil)
                        ExecutionService.start(context, waitUntil)
                        controller.destroy()
                    } else {
                        execute(resolvedVariables, tryNumber)
                    }
                }
                .fail {
                    controller.destroy()
                }
    }

    private fun execute(resolvedVariables: ResolvedVariables, tryNumber: Int) {
        showProgress()
        HttpRequester.executeShortcut(context, shortcut, resolvedVariables)
                .done { response ->
                    setLastResponse(response)
                    if (shortcut.isFeedbackErrorsOnly()) {
                        finishWithoutAnimation()
                    } else {
                        val simple = shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE
                        val output = if (simple) String.format(getString(R.string.executed), shortcut.getSafeName(context)) else generateOutputFromResponse(response)
                        displayOutput(output, response.contentType)
                    }
                }
                .fail { error ->
                    if (!shortcut.feedbackUsesUI() && shortcut.retryPolicy == Shortcut.RETRY_POLICY_WAIT_FOR_INTERNET && error.networkResponse == null) {
                        rescheduleExecution(resolvedVariables, tryNumber)
                        if (shortcut.feedback != Shortcut.FEEDBACK_NONE && tryNumber == 0) {
                            showToast(String.format(context.getString(R.string.execution_delayed), shortcut.getSafeName(context)), Toast.LENGTH_LONG)
                        }
                        finishWithoutAnimation()
                    } else {
                        setLastResponse(null)
                        val simple = shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE_ERRORS || shortcut.feedback == Shortcut.FEEDBACK_TOAST_SIMPLE
                        displayOutput(generateOutputFromError(error, simple), ShortcutResponse.TYPE_TEXT)
                    }
                }
                .always { _, _, _ ->
                    hideProgress()
                    controller.destroy()
                }
    }

    private fun rescheduleExecution(resolvedVariables: ResolvedVariables, tryNumber: Int) {
        if (tryNumber < MAX_RETRY) {
            val waitUntil = DateUtil.calculateDate(calculateDelay(tryNumber))
            controller.createPendingExecution(shortcut.id, resolvedVariables.toList(), tryNumber, waitUntil)
            ExecutionService.start(context, waitUntil)
        }
    }

    private fun calculateDelay(tryNumber: Int) = Math.pow(RETRY_BACKOFF, tryNumber.toDouble()).toInt() * 1000

    private fun generateOutputFromResponse(response: ShortcutResponse) = response.bodyAsString

    private fun generateOutputFromError(error: VolleyError, simple: Boolean): String {
        val name = shortcut.getSafeName(context)

        if (error.networkResponse != null) {
            val builder = StringBuilder()
            builder.append(String.format(getString(R.string.error_http), name, error.networkResponse.statusCode))

            if (!simple && error.networkResponse.data != null) {
                try {
                    builder.append("\n\n")
                    builder.append(String(error.networkResponse.data))
                } catch (e: Exception) {

                }
            }

            return builder.toString()
        } else {
            return if (error.cause?.message != null) {
                String.format(getString(R.string.error_other), name, error.cause!!.message)
            } else if (error.message != null) {
                String.format(getString(R.string.error_other), name, error.message)
            } else {
                String.format(getString(R.string.error_other), name, error.javaClass.simpleName)
            }
        }
    }

    private fun showProgress() {
        when (shortcut.feedback) {
            Shortcut.FEEDBACK_DIALOG -> {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(context, null, String.format(getString(R.string.progress_dialog_message), shortcut.getSafeName(context)))
                }
            }
            Shortcut.FEEDBACK_ACTIVITY -> {
                progressSpinner.visible = true
                responseTextContainer.visible = false
                formattedResponseText.visible = false
            }
        }
    }

    private fun hideProgress() {
        when (shortcut.feedback) {
            Shortcut.FEEDBACK_DIALOG -> {
                progressDialog?.dismiss()
                progressDialog = null
            }
            Shortcut.FEEDBACK_ACTIVITY -> {
                progressSpinner.visible = false
            }
        }
    }

    private fun displayOutput(output: String, type: String) {
        when (shortcut.feedback) {
            Shortcut.FEEDBACK_TOAST_SIMPLE, Shortcut.FEEDBACK_TOAST_SIMPLE_ERRORS -> {
                showToast(output, Toast.LENGTH_SHORT)
            }
            Shortcut.FEEDBACK_TOAST, Shortcut.FEEDBACK_TOAST_ERRORS -> {
                showToast(truncateIfNeeded(output, TOAST_MAX_LENGTH), Toast.LENGTH_LONG)
            }
            Shortcut.FEEDBACK_DIALOG -> {
                MaterialDialog.Builder(context)
                        .title(shortcut.getSafeName(context))
                        .content(output)
                        .positiveText(R.string.button_ok)
                        .dismissListener { finishWithoutAnimation() }
                        .show()
            }
            Shortcut.FEEDBACK_ACTIVITY -> {
                when (type) {
                    ShortcutResponse.TYPE_JSON -> {
                        formattedResponseText.setCode(GsonUtil.prettyPrint(output), "json")
                        formattedResponseText.visibility = View.VISIBLE
                    }
                    ShortcutResponse.TYPE_XML -> {
                        formattedResponseText.setCode(output, "xml")
                        formattedResponseText.visibility = View.VISIBLE
                    }
                    else -> {
                        responseText.text = output
                        responseTextContainer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showToast(message: String, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }

    private fun setLastResponse(response: ShortcutResponse?) {
        this.lastResponse = response
        invalidateOptionsMenu()
    }

    override fun finishWithoutAnimation() {
        hideProgress()
        super.finishWithoutAnimation()
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
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = ShortcutResponse.TYPE_TEXT
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, lastResponse!!.bodyAsString)
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)))
        } catch (e: Exception) {
            showToast(getString(R.string.error_share_failed), Toast.LENGTH_LONG)
            CrashReporting.logException(e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ExecutionService.start(context)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    companion object {

        const val ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.resolveVariablesAndExecute"
        const val EXTRA_SHORTCUT_ID = "id"
        const val EXTRA_VARIABLE_VALUES = "variable_values"
        const val EXTRA_TRY_NUMBER = "try_number"

        private const val MAX_RETRY = 5
        private const val RETRY_BACKOFF = 2.4

        private const val TOAST_MAX_LENGTH = 400

        private const val MAX_SHARE_LENGTH = 500000

        private fun truncateIfNeeded(string: String, maxLength: Int) =
                if (string.length > maxLength) string.substring(0, maxLength) + "…" else string
    }

}
