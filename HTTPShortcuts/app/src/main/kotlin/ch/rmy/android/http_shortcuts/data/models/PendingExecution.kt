package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import java.time.Instant

data class PendingExecution(
    val id: ExecutionId,
    val shortcutId: ShortcutId,
    val tryNumber: Int,
    val delayUntil: Instant?,
    val waitForNetwork: Boolean,
    val recursionDepth: Int,
    val resolvedVariables: Map<VariableKey, String>,
    val requestCode: Int,
    val type: PendingExecutionType,
)
