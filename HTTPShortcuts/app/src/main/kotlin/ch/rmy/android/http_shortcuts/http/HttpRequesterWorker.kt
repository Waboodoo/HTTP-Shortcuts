package ch.rmy.android.http_shortcuts.http

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class HttpRequesterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var httpRequester: HttpRequester

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun doWork(): Result {
        val params = getParams()
        val shortcut = try {
            shortcutRepository.getShortcutById(params.shortcutId)
        } catch (e: NoSuchElementException) {
            return Result.failure()
        }

        val response = try {
            httpRequester
                .executeShortcut(
                    context,
                    shortcut,
                    sessionId = params.sessionId,
                    variableValues = params.variableValues,
                    fileUploadResult = params.fileUploadResult,
                    useCookieJar = shortcut.acceptCookies,
                )
        } catch (e: Exception) {
            when (val failureOutput = shortcut.responseHandling?.failureOutput) {
                ResponseHandlingModel.FAILURE_OUTPUT_DETAILED,
                ResponseHandlingModel.FAILURE_OUTPUT_SIMPLE,
                -> {
                    displayResult(
                        shortcut,
                        generateOutputFromError(
                            e,
                            shortcut.getSafeName(context),
                            simple = failureOutput == ResponseHandlingModel.FAILURE_OUTPUT_SIMPLE,
                        ),
                        response = (e as? ErrorResponse)?.shortcutResponse,
                    )
                }
                else -> Unit
            }

            if (e !is IOException && e !is ErrorResponse) {
                logException(e)
                return Result.failure()
            }
            return Result.success()
        }

        handleDisplayingOfResult(shortcut, response, params.variableValues)

        return Result.success()
    }

    private fun getParams() =
        inputData.getString(DATA_SERIALIZED_PARAMS)!!
            .let {
                GsonUtil.gson.fromJson(it, Params::class.java)
            }

    private suspend fun handleDisplayingOfResult(shortcut: ShortcutModel, response: ShortcutResponse, variableValues: Map<VariableKey, String>) {
        when (shortcut.responseHandling!!.successOutput) {
            ResponseHandlingModel.SUCCESS_OUTPUT_MESSAGE -> {
                displayResult(
                    shortcut,
                    output = shortcut.responseHandling
                        ?.successMessage
                        ?.takeUnlessEmpty()
                        ?.let {
                            injectVariables(it, variableValues)
                        }
                        ?: context.getString(R.string.executed, shortcut.getSafeName(context)),
                    response = response,
                )
            }
            ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE -> displayResult(shortcut, output = null, response)
            ResponseHandlingModel.SUCCESS_OUTPUT_NONE -> Unit
        }
    }

    private fun injectVariables(string: String, variableValues: Map<VariableKey, String>): String =
        Variables.rawPlaceholdersToResolvedValues(string, variableValues)

    private suspend fun displayResult(shortcut: ShortcutModel, output: String?, response: ShortcutResponse? = null) {
        if (shortcut.responseHandling?.uiType != ResponseHandlingModel.UI_TYPE_TOAST) {
            return
        }
        withContext(Dispatchers.Main) {
            context.showToast(
                (output ?: response?.getContentAsString(context) ?: "")
                    .truncate(maxLength = TOAST_MAX_LENGTH)
                    .let(HTMLUtil::format)
                    .ifBlank { context.getString(R.string.message_blank_response) },
                long = shortcut.responseHandling?.successOutput == ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE
            )
        }
    }

    private fun generateOutputFromError(error: Throwable, shortcutName: String, simple: Boolean = false) =
        ErrorFormatter(context).getPrettyError(error, shortcutName, includeBody = !simple)

    private data class Params(
        val shortcutId: ShortcutId,
        val sessionId: String,
        val variableValues: Map<VariableKey, String>,
        val fileUploadResult: FileUploadManager.Result?,
    )

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke(
            shortcutId: ShortcutId,
            sessionId: String,
            variableValues: Map<VariableKey, String>,
            fileUploadResult: FileUploadManager.Result?,
        ) {
            val params = Params(shortcutId, sessionId, variableValues, fileUploadResult)
            with(WorkManager.getInstance(context)) {
                enqueue(
                    OneTimeWorkRequestBuilder<HttpRequesterWorker>()
                        .setInputData(
                            Data.Builder()
                                .putString(DATA_SERIALIZED_PARAMS, GsonUtil.gson.toJson(params))
                                .build()
                        )
                        .build()
                )
            }
        }
    }

    companion object {
        private const val DATA_SERIALIZED_PARAMS = "params"

        private const val TOAST_MAX_LENGTH = 400
    }
}
