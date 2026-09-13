package ch.rmy.android.http_shortcuts.tiles

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.appcompat.app.AlertDialog
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionFactory
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckHeadlessExecutionUseCase
import ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile.QuickSettingsTileActivity
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class QuickTileService : TileService() {

    private var _scope: CoroutineScope? = null
    private val scope: CoroutineScope
        get() = synchronized(this) {
            val scope = _scope
            if (scope != null && scope.isActive) {
                return scope
            }
            _scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            return _scope!!
        }

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var requestHeaderRepository: RequestHeaderRepository

    @Inject
    lateinit var requestParameterRepository: RequestParameterRepository

    @Inject
    lateinit var executionFactory: ExecutionFactory

    @Inject
    lateinit var checkHeadlessExecution: CheckHeadlessExecutionUseCase

    override fun onClick() {
        scope.launch {
            val shortcuts = getShortcuts()
            val shortcutIds = shortcuts.ids()
            handleShortcuts(
                shortcuts = shortcuts,
                headersByShortcutId = requestHeaderRepository.getRequestHeadersByShortcutIds(shortcutIds),
                parametersByShortcutId = requestParameterRepository.getRequestParametersByShortcutIds(shortcutIds),
            )
        }
    }

    private suspend fun getShortcuts() =
        shortcutRepository.getQuickSettingsShortcuts()

    private fun handleShortcuts(
        shortcuts: List<Shortcut>,
        headersByShortcutId: Map<ShortcutId, List<RequestHeader>>,
        parametersByShortcutId: Map<ShortcutId, List<RequestParameter>>,
    ) {
        shortcuts.singleOrNull()
            ?.let { shortcut ->
                executeShortcut(shortcut, headersByShortcutId[shortcut.id] ?: emptyList(), parametersByShortcutId[shortcut.id] ?: emptyList())
            }
            ?: run {
                if (shortcuts.isNotEmpty() &&
                    shortcuts.all {
                        canRunWithoutExecuteActivity(
                            it,
                            headersByShortcutId[it.id],
                            parametersByShortcutId[it.id],
                        )
                    }
                ) {
                    setTheme(com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar)
                    tryOrLog {
                        if (isLocked) {
                            unlockAndRun {
                                showDialog(shortcuts, headersByShortcutId, parametersByShortcutId)
                            }
                        } else {
                            showDialog(shortcuts, headersByShortcutId, parametersByShortcutId)
                        }
                    }
                } else {
                    QuickSettingsTileActivity.IntentBuilder()
                        .build(context)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .let { intent ->
                            startIntent(intent)
                        }
                }
            }
    }

    private fun showDialog(
        shortcuts: List<Shortcut>,
        headersByShortcutId: Map<ShortcutId, List<RequestHeader>>,
        parametersByShortcutId: Map<ShortcutId, List<RequestParameter>>,
    ) {
        showDialog(
            AlertDialog.Builder(context)
                .setItems(shortcuts.map { it.name }.toTypedArray()) { _, index ->
                    val shortcut = shortcuts[index]
                    executeShortcut(shortcut, headersByShortcutId[shortcut.id], parametersByShortcutId[shortcut.id])
                }
                .create(),
        )
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startIntent(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    private fun executeShortcut(
        shortcut: Shortcut,
        requestHeaders: List<RequestHeader>?,
        requestParameters: List<RequestParameter>?,
    ) {
        if (canRunWithoutExecuteActivity(shortcut, requestHeaders, requestParameters)) {
            scope.launch {
                executionFactory.createExecution(
                    ExecutionParams(
                        shortcutId = shortcut.id,
                        trigger = ShortcutTriggerType.QUICK_SETTINGS_TILE,
                        triggeredAt = Instant.now(),
                    ),
                    dialogHandle = object : DialogHandle {
                        override suspend fun <T : Any> showDialog(dialogState: ExecuteDialogState<T>): T {
                            logException(IllegalStateException("Headless quick service tile execution tried showing a dialog"))
                            throw CancellationException()
                        }
                    },
                )
                    .execute()
                    .collect()
            }
            return
        }

        ExecuteActivity.IntentBuilder(shortcut.id)
            .trigger(ShortcutTriggerType.QUICK_SETTINGS_TILE)
            .build(context)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { intent ->
                startIntent(intent)
            }
    }

    private fun canRunWithoutExecuteActivity(
        shortcut: Shortcut,
        requestHeaders: List<RequestHeader>?,
        requestParameters: List<RequestParameter>?,
    ): Boolean {
        if (shortcut.confirmationType != null) {
            return false
        }
        if (shortcut.codeOnPrepare.isNotEmpty()) {
            return false
        }
        if (!checkHeadlessExecution.invoke(shortcut, requestParameters ?: emptyList())) {
            return false
        }
        val globalVariableIds = VariableResolver.findResolvableVariableIdentifiersExcludingScripting(
            shortcut = shortcut,
            headers = requestHeaders ?: emptyList(),
            parameters = requestParameters ?: emptyList(),
        )
        // If a shortcut uses any variables, we cannot know whether those variables can be resolved
        // without the ExecuteActivity being present, so we have to err on the side of caution.
        return globalVariableIds.isEmpty()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateIcon()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateIcon()
    }

    private fun updateIcon() {
        scope.launch {
            val qsTile = qsTile ?: return@launch
            val shortcuts = getShortcuts()
            val shortcut = shortcuts.singleOrNull()
            if (shortcut != null) {
                qsTile.label = shortcut.name
                qsTile.icon = shortcut.icon
                    .takeIf { it.isUsableAsSilhouette }
                    ?.let {
                        withContext(Dispatchers.IO) {
                            IconUtil.getIcon(context, it, adaptive = false)
                        }
                    }
                    ?: Icon.createWithResource(context, R.drawable.ic_quick_settings_tile)
            } else {
                qsTile.label = getString(R.string.action_quick_settings_tile_trigger)
                qsTile.icon = Icon.createWithResource(context, R.drawable.ic_quick_settings_tile)
            }
            qsTile.state = Tile.STATE_ACTIVE
            qsTile.updateTile()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope?.cancel()
        _scope = null
    }
}
