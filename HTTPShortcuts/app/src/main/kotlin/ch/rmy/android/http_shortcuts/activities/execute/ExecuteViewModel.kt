package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Application
import android.os.SystemClock
import androidx.annotation.StringRes
import ch.rmy.android.framework.extensions.attachTo
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
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
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

    private val fileIds: List<String> by lazy {
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

        if (initData.executionId != null) {
            pendingExecutionsRepository.removePendingExecution(initData.executionId!!)
        } else {
            Completable.complete()
        }
            .andThen(loadData())
            .subscribe(
                ::onDataLoaded,
                ::onDataLoadError,
            )
            .attachTo(destroyer)
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

    private fun loadData(): Completable {
        val loadGlobalCode = appRepository.getGlobalCode()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { globalCode ->
                this.globalCode = globalCode
            }
            .ignoreElement()

        val loadShortcut = shortcutRepository.getShortcutById(initData.shortcutId)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { shortcut ->
                this.shortcut = shortcut
            }
            .ignoreElement()

        return Completable.merge(listOf(loadGlobalCode, loadShortcut))
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
        dialogState = DialogState.create(id = "execution-error") {
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
