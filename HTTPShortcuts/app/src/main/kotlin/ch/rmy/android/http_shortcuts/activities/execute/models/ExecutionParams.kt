package ch.rmy.android.http_shortcuts.activities.execute.models

import android.net.Uri
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import java.time.Instant

data class ExecutionParams(
    val shortcutId: ShortcutId,
    val variableValues: Map<VariableKeyOrId, String> = emptyMap(),
    val executionId: ExecutionId? = null,
    val tryNumber: Int = 0,
    val recursionDepth: Int = 0,
    val fileUris: List<Uri> = emptyList(),
    val trigger: ShortcutTriggerType? = null,
    val triggeredAt: Instant = Instant.now(),
    val isNested: Boolean = false,
)
