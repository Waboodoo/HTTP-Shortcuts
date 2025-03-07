package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import android.content.Intent
import android.net.Uri
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
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
        variableValues: Map<VariableKey, String> = emptyMap(),
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
                context.startForegroundService(
                    Intent(context, ExecutionService::class.java)
                        .putExtras(intent),
                )
            } else {
                intent.startActivity(context)
            }
        }
    }
}
