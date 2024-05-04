package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.utils.Settings
import javax.inject.Inject

class ExecutionStarter
@Inject
constructor(
    private val context: Context,
    private val executeWorkerStarter: ExecuteWorker.Starter,
    private val settings: Settings,
) {
    fun execute(
        shortcutId: ShortcutId,
        trigger: ShortcutTriggerType,
        variableValues: Map<VariableKey, String> = emptyMap(),
        fileUris: List<Uri> = emptyList(),
    ) {
        if (settings.useExperimentalExecutionMode) {
            executeWorkerStarter.invoke(
                ExecutionParams(
                    shortcutId = shortcutId,
                    trigger = trigger,
                    variableValues = variableValues,
                    fileUris = fileUris,
                )
            )
        } else {
            ExecuteActivity.IntentBuilder(shortcutId)
                .runIfNotNull(trigger) {
                    trigger(it)
                }
                .variableValues(variableValues)
                .files(fileUris)
                .startActivity(context)
        }
    }
}
