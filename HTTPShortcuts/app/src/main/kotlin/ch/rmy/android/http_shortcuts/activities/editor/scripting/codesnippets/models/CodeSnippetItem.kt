package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
data class CodeSnippetItem(
    val title: Localizable,
    val description: Localizable?,
    val docRef: String?,
    val keywords: Set<String> = emptySet(),
    val action: () -> Unit,
)
