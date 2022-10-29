package ch.rmy.android.http_shortcuts.activities.execute.models

import android.net.Uri
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

data class ExecutionParams(
    val shortcutId: ShortcutId,
    val variableValues: Map<String, String>,
    val executionId: String?,
    val tryNumber: Int,
    val recursionDepth: Int,
    val fileUris: List<Uri>,
)
