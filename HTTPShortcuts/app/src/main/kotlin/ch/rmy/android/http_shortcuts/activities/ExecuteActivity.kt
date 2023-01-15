package ch.rmy.android.http_shortcuts.activities

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.doOnDestroy
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.getParcelableList
import ch.rmy.android.framework.extensions.getSerializable
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteEvent
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteViewModel
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.history.HistoryCleanUpWorker
import ch.rmy.android.http_shortcuts.scheduling.ExecutionSchedulerWorker
import ch.rmy.android.http_shortcuts.utils.CacheFilesCleanupWorker
import ch.rmy.android.http_shortcuts.utils.ProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExecuteActivity : BaseActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    override val supportsSnackbars: Boolean
        get() = false

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var cacheFilesCleanupStarter: CacheFilesCleanupWorker.Starter

    @Inject
    lateinit var executionSchedulerStarter: ExecutionSchedulerWorker.Starter

    @Inject
    lateinit var historyCleanUpStarter: HistoryCleanUpWorker.Starter

    private val viewModel: ExecuteViewModel by bindViewModel()

    private var isLowMemory = false

    private val progressIndicator: ProgressIndicator by lazy {
        ProgressIndicator(this)
            .also { progressIndicator ->
                doOnDestroy(progressIndicator::destroy)
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        lifecycleScope.launch {
            pendingExecutionsRepository
                .createPendingExecution(
                    shortcutId = intent.extractShortcutId(),
                    resolvedVariables = intent.extractVariableValues(),
                    tryNumber = 0,
                    type = PendingExecutionType.NEW_INTENT,
                )
        }
    }

    override fun onCreated(savedState: Bundle?) {
        getApplicationComponent().inject(this)
        setTheme(themeHelper.transparentTheme)

        viewModel.initialize(
            ExecutionParams(
                shortcutId = intent.extractShortcutId(),
                variableValues = intent.extractVariableValues(),
                executionId = intent.extras?.getString(EXTRA_EXECUTION_SCHEDULE_ID),
                tryNumber = intent.extras?.getInt(EXTRA_TRY_NUMBER) ?: 0,
                recursionDepth = intent.extras?.getInt(EXTRA_RECURSION_DEPTH) ?: 0,
                fileUris = intent.getParcelableList(EXTRA_FILES) ?: emptyList(),
                trigger = ShortcutTriggerType.parse(intent.extras?.getString(EXTRA_TRIGGER)),
            )
        )

        initViewModelBindings()
    }

    override fun finish() {
        excludeFromRecents()
        executionSchedulerStarter()
        if (!isLowMemory) {
            tryOrLog {
                cacheFilesCleanupStarter()
                historyCleanUpStarter()
            }
        }
        super.finish()
    }

    private fun excludeFromRecents() {
        getSystemService<ActivityManager>()
            ?.let { activityManager ->
                activityManager.appTasks
                    .firstOrNull()
                    ?.setExcludeFromRecents(true)
            }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        isLowMemory = true
    }

    private fun initViewModelBindings() {
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
            is ExecuteEvent.ShowProgress -> {
                progressIndicator.showProgressDelayed(INVISIBLE_PROGRESS_THRESHOLD)
            }
            is ExecuteEvent.HideProgress -> {
                progressIndicator.hideProgress()
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onBackPressed() {
        finishWithoutAnimation()
    }

    class IntentBuilder(shortcutId: ShortcutId) : BaseIntentBuilder(ExecuteActivity::class) {

        init {
            intent.action = ACTION_EXECUTE_SHORTCUT
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
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

        fun executionId(id: ExecutionId) = also {
            intent.putExtra(EXTRA_EXECUTION_SCHEDULE_ID, id)
        }

        fun trigger(trigger: ShortcutTriggerType) = also {
            intent.putExtra(EXTRA_TRIGGER, trigger.name)
        }
    }

    companion object {

        private const val ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute"

        private const val EXTRA_SHORTCUT_ID = "id"
        private const val EXTRA_VARIABLE_VALUES = "variable_values"
        private const val EXTRA_TRY_NUMBER = "try_number"
        private const val EXTRA_RECURSION_DEPTH = "recursion_depth"
        private const val EXTRA_FILES = "files"
        private const val EXTRA_EXECUTION_SCHEDULE_ID = "schedule_id"
        private const val EXTRA_TRIGGER = "trigger"

        private const val INVISIBLE_PROGRESS_THRESHOLD = 400L

        fun Intent.extractShortcutId(): ShortcutId =
            getStringExtra(EXTRA_SHORTCUT_ID)
                ?: data?.lastPathSegment
                ?: ""

        fun Intent.extractVariableValues(): Map<VariableKey, String> =
            getSerializable<HashMap<VariableKey, String>>(EXTRA_VARIABLE_VALUES)
                ?: emptyMap()
    }
}
