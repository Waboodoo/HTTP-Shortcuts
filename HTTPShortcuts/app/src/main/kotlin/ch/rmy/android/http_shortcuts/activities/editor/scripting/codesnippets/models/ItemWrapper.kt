package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models

import androidx.compose.runtime.Stable

@Stable
sealed interface ItemWrapper {
    val id: String

    @Stable
    data class Section(
        override val id: String,
        val sectionItem: SectionItem,
        val expanded: Boolean,
    ) : ItemWrapper

    @Stable
    data class CodeSnippet(
        override val id: String,
        val codeSnippetItem: CodeSnippetItem,
    ) : ItemWrapper
}
