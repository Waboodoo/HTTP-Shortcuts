package ch.rmy.android.http_shortcuts.activities.execute.models

import android.net.Uri
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey

data class ExecutionParams(
    val shortcutId: ShortcutId,
    val variableValues: Map<VariableKey, String> = emptyMap(),
    val executionId: ExecutionId? = null,
    val tryNumber: Int = 0,
    val recursionDepth: Int = 0,
    val fileUris: List<Uri> = emptyList(),
    val isNested: Boolean = false,
)
