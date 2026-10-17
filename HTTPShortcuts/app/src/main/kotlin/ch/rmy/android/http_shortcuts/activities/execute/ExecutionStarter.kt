package ch.rmy.android.http_shortcuts.activities.execute

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.extensions.shouldUseForegroundService
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExecutionStarter
@Inject
constructor(
    private val context: Context,
    private val shortcutRepository: ShortcutRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun execute(
        shortcutId: ShortcutId,
        trigger: ShortcutTriggerType,
        variableValues: Map<VariableKeyOrId, String> = emptyMap(),
        fileUris: List<Uri> = emptyList(),
    ) {
        scope.launch {
            val intent = ExecuteActivity.IntentBuilder(shortcutId)
                .runIfNotNull(trigger) {
                    trigger(it)
                }
                .variableValues(variableValues)
                .files(fileUris)
                .build(context)

            if (shortcutRepository.shouldUseForegroundService(shortcutId)) {
                logInfo("Starting execution using foreground service")
                try {
                    context.startForegroundService(
                        Intent(context, ExecutionService::class.java)
                            .putExtras(intent),
                    )
                } catch (e: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                        logInfo("Starting in foreground failed, falling back to activity")
                        intent.startActivity(context)
                    } else {
                        throw e
                    }
                }
            } else {
                logInfo("Starting execution using activity")
                intent.startActivity(context)
            }
        }
    }
}
