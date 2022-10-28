package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Application
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ExecuteViewModel(
    application: Application,
) : BaseViewModel<ExecuteViewModel.InitData, ExecuteViewState>(application), WithDialog {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var appRepository: AppRepository

    private lateinit var globalCode: String
    private lateinit var shortcut: ShortcutModel

    private val fileIds: List<String> by lazy(LazyThreadSafetyMode.NONE) {
        // TODO: Move this into a use case?
        initData.variableValues["\$files"]
            ?.trim('[', ']')
            ?.split(",")
            ?.map { it.trim(' ', '"') }
            ?: emptyList()
    }

    init {
        getApplicationComponent().inject(this)
    }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ExecuteViewState()

    override fun onInitializationStarted(data: InitData) {
        if (isRepetition()) {
            finalizeInitialization(silent = true)
            finishWithoutAnimation()
            return
        }
        lastExecutionTime = SystemClock.elapsedRealtime()
        lastExecutionData = data

        viewModelScope.launch {
            try {
                initData.executionId?.let {
                    pendingExecutionsRepository.removePendingExecution(it)
                }
                loadData()
                onDataLoaded()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onDataLoadError(e)
            }
        }
    }

    private fun isRepetition(): Boolean {
        val time = lastExecutionTime ?: return false
        val data = lastExecutionData ?: return false
        return data.executionId == null &&
            initData.executionId == null &&
            data.shortcutId == initData.shortcutId &&
            data.variableValues == initData.variableValues &&
            SystemClock.elapsedRealtime() - time < REPETITION_DEBOUNCE_TIME.inWholeMilliseconds
    }

    private suspend fun loadData() {
        coroutineScope {
            val globalCodeDeferred = async {
                appRepository.getGlobalCode()
            }
            val shortcutDeferred = async {
                shortcutRepository.getShortcutById(initData.shortcutId)
            }
            this@ExecuteViewModel.globalCode = globalCodeDeferred.await()
            this@ExecuteViewModel.shortcut = shortcutDeferred.await()
        }
    }

    private fun onDataLoaded() {
        finalizeInitialization(silent = true)
        emitEvent(
            ExecuteEvent.Execute(
                shortcut = shortcut,
                variableValues = initData.variableValues,
                fileIds = fileIds,
                globalCode = globalCode,
            )
        )
    }

    private fun onDataLoadError(error: Throwable) {
        finalizeInitialization(silent = true)
        when (error) {
            is NoSuchElementException -> {
                showErrorDialog(R.string.shortcut_not_found)
            }
            else -> {
                showErrorDialog(R.string.error_generic)
                logException(error)
            }
        }
    }

    private fun showErrorDialog(@StringRes message: Int) {
        dialogState = createDialogState(id = "execution-error") {
            title(R.string.dialog_title_error)
                .message(message)
                .positive(R.string.dialog_ok)
                .dismissListener(::finishWithoutAnimation)
                .build()
        }
    }

    private fun finishWithoutAnimation() {
        emitEvent(ViewModelEvent.Finish(skipAnimation = true))
    }

    data class InitData(
        val shortcutId: ShortcutId,
        val variableValues: Map<String, String>,
        val executionId: String?,
    )

    companion object {
        private val REPETITION_DEBOUNCE_TIME = 500.milliseconds

        private var lastExecutionTime: Long? = null
        private var lastExecutionData: InitData? = null
    }
}
