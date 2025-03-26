package ch.rmy.android.http_shortcuts.activities

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.getParcelableList
import ch.rmy.android.framework.extensions.getSerializable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogs
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteViewModel
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionService
import ch.rmy.android.http_shortcuts.activities.execute.StartServiceEvent
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExecuteActivity : BaseComposeActivity() {

    override val initializeWithTheme: Boolean
        get() = false

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var settings: Settings

    private val viewModel: ExecuteViewModel by viewModels()

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
        super.onCreated(savedState)
        viewModel.init(intent.toExecutionParams())
        initViewModelBindings()
    }

    @Composable
    override fun Content() {
        val viewState by viewModel.viewStateFlow.collectAsStateWithLifecycle()

        var showProgressSpinner by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(viewState?.progressSpinnerVisible == true) {
            showProgressSpinner = if (viewState?.progressSpinnerVisible == true) {
                delay(400.milliseconds)
                true
            } else {
                false
            }
        }

        BackHandler {
            finishWithoutAnimation()
        }

        if (showProgressSpinner && viewState?.dialogState == null) {
            ProgressDialog(
                onDismissRequest = {
                    finishWithoutAnimation()
                },
            )
        }

        ExecuteDialogs(
            viewState?.dialogState,
            onResult = viewModel::onDialogResult,
            onDismissed = viewModel::onDialogDismissed,
        )
    }

    override fun finish() {
        excludeFromRecents()
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

    private fun initViewModelBindings() {
        lifecycleScope.launch {
            viewModel.events.collect(::handleEvent)
        }
    }

    override fun handleEvent(event: ViewModelEvent) {
        if (event is StartServiceEvent) {
            startForegroundService(
                Intent(context, ExecutionService::class.java)
                    .putExtras(intent),
            )
        } else {
            super.handleEvent(event)
        }
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
            if (variableValues.isNotEmpty()) {
                intent.putExtra(EXTRA_VARIABLE_VALUES, HashMap(variableValues))
            }
        }

        fun recursionDepth(recursionDepth: Int) = also {
            intent.putExtra(EXTRA_RECURSION_DEPTH, recursionDepth)
        }

        fun files(files: List<Uri>) = also {
            if (files.isNotEmpty()) {
                intent.putParcelableArrayListExtra(
                    EXTRA_FILES,
                    ArrayList<Uri>().apply { addAll(files) },
                )
            }
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

        fun Intent.extractShortcutId(): ShortcutId =
            getStringExtra(EXTRA_SHORTCUT_ID)
                ?: data?.lastPathSegment
                ?: ""

        fun Intent.extractVariableValues(): Map<VariableKey, String> =
            getSerializable<HashMap<VariableKey, String>>(EXTRA_VARIABLE_VALUES)
                ?: emptyMap()

        fun Intent.toExecutionParams(): ExecutionParams =
            ExecutionParams(
                shortcutId = extractShortcutId(),
                variableValues = extractVariableValues(),
                executionId = extras?.getInt(EXTRA_EXECUTION_SCHEDULE_ID),
                tryNumber = extras?.getInt(EXTRA_TRY_NUMBER) ?: 0,
                recursionDepth = extras?.getInt(EXTRA_RECURSION_DEPTH) ?: 0,
                fileUris = getParcelableList(EXTRA_FILES) ?: emptyList(),
                trigger = extras?.getString(EXTRA_TRIGGER)?.let { ShortcutTriggerType.parse(it) },
            )
    }
}
