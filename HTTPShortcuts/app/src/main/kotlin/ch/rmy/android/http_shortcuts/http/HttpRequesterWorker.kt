package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.usecases.ShowResultNotificationUseCase
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinRepository
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.extensions.getRequestHeadersForShortcut
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcut
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class HttpRequesterWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val shortcutRepository: ShortcutRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val certificatePinRepository: CertificatePinRepository,
    private val workingDirectoryRepository: WorkingDirectoryRepository,
    private val httpRequester: HttpRequester,
    private val errorFormatter: ErrorFormatter,
    private val showResultNotification: ShowResultNotificationUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val params = getParams()
        val shortcut = try {
            shortcutRepository.getShortcutById(params.shortcutId)
        } catch (_: NoSuchElementException) {
            return Result.failure()
        }

        val response = try {
            httpRequester
                .executeShortcut(
                    context,
                    shortcut = shortcut,
                    headers = requestHeaderRepository.getRequestHeadersForShortcut(shortcut),
                    parameters = requestParameterRepository.getRequestParametersForShortcut(shortcut),
                    storeDirectoryUri = shortcut.responseStoreDirectoryId
                        ?.let { workingDirectoryId ->
                            try {
                                workingDirectoryRepository.getWorkingDirectoryById(workingDirectoryId).directory
                            } catch (_: NoSuchElementException) {
                                null
                            }
                        },
                    sessionId = params.sessionId,
                    variableValues = params.variableValues,
                    fileUploadResult = params.fileUploadResult,
                    useCookieJar = shortcut.acceptCookies,
                    certificatePins = certificatePinRepository.getCertificatePins(),
                )
        } catch (e: Exception) {
            when (val failureOutput = shortcut.responseFailureOutput) {
                ResponseFailureOutput.DETAILED,
                ResponseFailureOutput.SIMPLE,
                -> {
                    displayResult(
                        shortcut,
                        generateOutputFromError(
                            e,
                            shortcut.getSafeName(context),
                            simple = failureOutput == ResponseFailureOutput.SIMPLE,
                        ),
                        response = (e as? ErrorResponse)?.shortcutResponse,
                    )
                }
                else -> Unit
            }

            if (e !is IOException && e !is ErrorResponse && e !is UserException) {
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

    private suspend fun handleDisplayingOfResult(shortcut: Shortcut, response: ShortcutResponse, variableValues: Map<VariableId, String>) {
        when (shortcut.responseSuccessOutput) {
            ResponseSuccessOutput.MESSAGE -> {
                displayResult(
                    shortcut,
                    output = shortcut.responseSuccessMessage
                        .takeUnlessEmpty()
                        ?.let {
                            injectVariables(it, variableValues)
                        }
                        ?: context.getString(R.string.executed, shortcut.getSafeName(context)),
                    response = response,
                )
            }
            ResponseSuccessOutput.RESPONSE -> displayResult(shortcut, output = null, response)
            ResponseSuccessOutput.NONE -> Unit
        }
    }

    private fun injectVariables(string: String, variableValues: Map<VariableId, String>): String =
        Variables.rawPlaceholdersToResolvedValues(string, variableValues)

    private suspend fun displayResult(shortcut: Shortcut, output: String?, response: ShortcutResponse? = null) {
        when (shortcut.responseUiType) {
            ResponseUiType.TOAST -> {
                withContext(Dispatchers.Main) {
                    context.showToast(
                        (output ?: response?.getContentAsString(context) ?: "")
                            .truncate(maxLength = TOAST_MAX_LENGTH)
                            .let(HTMLUtil::toSpanned)
                            .ifBlank { context.getString(R.string.message_blank_response) },
                        long = shortcut.responseSuccessOutput == ResponseSuccessOutput.RESPONSE,
                    )
                }
            }
            ResponseUiType.NOTIFICATION -> {
                showResultNotification(shortcut, response, output)
            }
            else -> Unit
        }
    }

    private fun generateOutputFromError(error: Throwable, shortcutName: String, simple: Boolean = false) =
        errorFormatter.getPrettyError(error, shortcutName, includeBody = !simple)

    private data class Params(
        val shortcutId: ShortcutId,
        val sessionId: String,
        val variableValues: Map<VariableId, String>,
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
            variableValues: Map<VariableId, String>,
            fileUploadResult: FileUploadManager.Result?,
        ) {
            val params = Params(shortcutId, sessionId, variableValues, fileUploadResult)
            with(WorkManager.getInstance(context)) {
                enqueue(
                    OneTimeWorkRequestBuilder<HttpRequesterWorker>()
                        .runIf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        }
                        .setInputData(
                            Data.Builder()
                                .putString(DATA_SERIALIZED_PARAMS, GsonUtil.gson.toJson(params))
                                .build(),
                        )
                        .build(),
                )
            }
        }
    }

    companion object {
        private const val DATA_SERIALIZED_PARAMS = "params"

        private const val TOAST_MAX_LENGTH = 400
    }
}
