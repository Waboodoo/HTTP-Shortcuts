package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import ch.rmy.android.framework.utils.localization.Localizable

data class CodeSnippetItem(
    val title: Localizable,
    val description: Localizable?,
    val docRef: String?,
    val action: () -> Unit,
)
