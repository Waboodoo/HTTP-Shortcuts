package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
data class SectionItem(
    val title: Localizable,
    @DrawableRes val icon: Int,
    val codeSnippetItems: List<CodeSnippetItem>,
)
