package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases

import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models.CodeSnippetItem
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models.ItemWrapper
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models.SectionItem
import ch.rmy.android.http_shortcuts.utils.SearchUtil.normalizeToKeywords
import javax.inject.Inject

class GetItemWrappersUseCase
@Inject
constructor() {

    operator fun invoke(sectionItems: List<SectionItem>, expandedSections: Set<String>, query: String?): List<ItemWrapper> =
        sectionItems
            .flatMapIndexed { sectionIndex, item ->
                val id = sectionIndex.toString()
                val queryTerms = query?.trim()?.takeUnlessEmpty()?.let { normalizeToKeywords(it, minLength = 1) }
                val expanded = queryTerms != null || expandedSections.contains(id)
                listOf<ItemWrapper>(
                    ItemWrapper.Section(
                        id = id,
                        sectionItem = item,
                        expanded = expanded,
                    ),
                )
                    .runIf(expanded) {
                        plus(
                            item.codeSnippetItems
                                .mapIndexed { codeSnippetIndex, codeSnippetItem ->
                                    ItemWrapper.CodeSnippet(
                                        id = "$sectionIndex.$codeSnippetIndex",
                                        codeSnippetItem = codeSnippetItem,
                                    )
                                }
                                .runIfNotNull(queryTerms) {
                                    filter { codeSnippetItemWrapper ->
                                        codeSnippetItemWrapper.codeSnippetItem.matches(queryTerms!!)
                                    }
                                },
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
