package ch.rmy.android.http_shortcuts.tiles

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.TileService
import androidx.appcompat.app.AlertDialog
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
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
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class QuickTileService : TileService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

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
        if (!scope.isActive) {
            logException(IllegalStateException("QuickTileService coroutine scope was inactive"))
            val shortcuts = runBlocking {
                getShortcuts()
            }
            val shortcutIds = shortcuts.ids()
            val headersByShortcutId = runBlocking {
                requestHeaderRepository.getRequestHeadersByShortcutIds(shortcutIds)
            }
            val parametersByShortcutIds = runBlocking {
                requestParameterRepository.getRequestParametersByShortcutIds(shortcutIds)
            }
            handleShortcuts(shortcuts, headersByShortcutId, parametersByShortcutIds)
            return
        }
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
                if (shortcuts.isNotEmpty() && shortcuts.all {
                        canRunWithoutExecuteActivity(
                            it,
                            headersByShortcutId[it.id],
                            parametersByShortcutId[it.id],
                        )
                    }
                ) {
                    setTheme(com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar)
                    showDialog(
                        AlertDialog.Builder(context)
                            .setItems(shortcuts.map { it.name }.toTypedArray()) { _, index ->
                                val shortcut = shortcuts[index]
                                executeShortcut(shortcut, headersByShortcutId[shortcut.id], parametersByShortcutId[shortcut.id])
                            }
                            .create(),
                    )
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
        val variableIds = VariableResolver.extractVariableIdsExcludingScripting(
            shortcut = shortcut,
            headers = requestHeaders ?: emptyList(),
            parameters = requestParameters ?: emptyList(),
        )
        // If a shortcut uses any variables, we cannot know whether those variables can be resolved
        // without the ExecuteActivity being present, so we have to err on the side of caution.
        return variableIds.isEmpty()
    }

    override fun onStartListening() {
        super.onStartListening()
        scope.launch {
            val shortcuts = getShortcuts()
            val shortcut = shortcuts.singleOrNull()
            if (shortcut != null) {
                qsTile?.label = shortcut.name
                qsTile?.icon = (shortcut.icon as? ShortcutIcon.BuiltInIcon)
                    ?.takeIf { it.isUsableAsSilhouette }
                    ?.let {
                        IconUtil.getIcon(context, it, adaptive = false)
                    }
                    ?: Icon.createWithResource(context, R.drawable.ic_quick_settings_tile)
            } else {
                qsTile?.label = getString(R.string.action_quick_settings_tile_trigger)
                qsTile?.icon = Icon.createWithResource(context, R.drawable.ic_quick_settings_tile)
            }
            qsTile?.updateTile()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
