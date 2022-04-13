package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import androidx.annotation.DrawableRes
import ch.rmy.android.framework.utils.localization.Localizable

data class SectionItem(
    val title: Localizable,
    @DrawableRes val icon: Int,
    val codeSnippetItems: List<CodeSnippetItem>,
)
