package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

sealed interface ItemWrapper {
    data class Section(
        val id: Int,
        val sectionItem: SectionItem,
        val expanded: Boolean,
    ) : ItemWrapper

    data class CodeSnippet(
        val id: Int,
        val codeSnippetItem: CodeSnippetItem,
    ) : ItemWrapper
}
