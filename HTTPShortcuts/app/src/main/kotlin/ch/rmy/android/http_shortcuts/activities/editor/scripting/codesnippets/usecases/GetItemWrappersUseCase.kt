package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases

import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetItem
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.ItemWrapper
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.SectionItem
import ch.rmy.android.http_shortcuts.utils.SearchUtil.normalizeToKeywords
import javax.inject.Inject

class GetItemWrappersUseCase
@Inject
constructor() {

    operator fun invoke(sectionItems: List<SectionItem>, expandedSections: Set<Int>, query: String?): List<ItemWrapper> =
        sectionItems
            .flatMapIndexed { sectionIndex, item ->
                val queryTerms = query?.trim()?.takeUnlessEmpty()?.let { normalizeToKeywords(it, minLength = 1) }
                val expanded = queryTerms != null || expandedSections.contains(sectionIndex)
                listOf<ItemWrapper>(
                    ItemWrapper.Section(
                        id = sectionIndex,
                        sectionItem = item,
                        expanded = expanded,
                    )
                )
                    .runIf(expanded) {
                        plus(
                            item.codeSnippetItems
                                .mapIndexed { codeSnippetIndex, codeSnippetItem ->
                                    ItemWrapper.CodeSnippet(
                                        id = codeSnippetIndex + sectionIndex * 1000,
                                        codeSnippetItem = codeSnippetItem,
                                    )
                                }
                                .runIfNotNull(queryTerms) {
                                    filter { codeSnippetItemWrapper ->
                                        codeSnippetItemWrapper.codeSnippetItem.matches(queryTerms!!)
                                    }
                                }
                        )
                    }
                    .takeIf { queryTerms == null || it.size > 1 }
                    ?: emptyList()
            }

    private fun CodeSnippetItem.matches(queryTerms: Collection<String>): Boolean =
        keywords.any { keyword ->
            queryTerms.any { queryTerm ->
                keyword.contains(queryTerm) || (keyword.length >= 4 && queryTerm.length >= 4 && queryTerm.contains(keyword))
            }
        }
}
