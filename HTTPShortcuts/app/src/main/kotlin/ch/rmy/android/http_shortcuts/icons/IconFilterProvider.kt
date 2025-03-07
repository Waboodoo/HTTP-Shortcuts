package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.SearchUtil
import java.util.zip.GZIPInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IconFilterProvider(private val context: Context) {

    suspend fun init() {
        if (iconKeywords.isNotEmpty()) {
            return
        }
        logInfo("Initializing IconFilterProvider")
        tryOrLog {
            iconKeywords = withContext(Dispatchers.IO) {
                buildMap {
                    GZIPInputStream(context.assets.open("icons_keywords"))
                        .bufferedReader()
                        .forEachLine { line ->
                            val words = line.split(" ")
                            val plainName = words.first()
                            val keywords = words
                                .flatMap { word ->
                                    setOf(word.filter { it != '_' }) + word.split('_')
                                }
                                .toSet()
                            val key = plainName.filter { it != '_' }
                            val existingKeywords = get(key)
                            put(key, keywords.runIfNotNull(existingKeywords) { plus(it) })
                        }
                }
            }

            logInfo("IconFilterProvider initialized")
        }
    }

    fun createScoringFunction(query: String): ((ShortcutIcon.BuiltInIcon) -> Int)? {
        val queryTerms = query.trim()
            .takeUnlessEmpty()
            ?.let { SearchUtil.normalizeToKeywords(it, minLength = 1) }
            ?: return null
        return { icon ->
            icon.score(queryTerms)
        }
    }

    private fun ShortcutIcon.BuiltInIcon.score(queryTerms: Collection<String>): Int {
        val primaryKeywords = SearchUtil.normalizeToKeywords(plainName, minLength = 1)
        val keywords = iconKeywords[plainName.filter { it != '_' }]
            ?: primaryKeywords
        var score = 0
        keywords.forEach { keyword ->
            queryTerms.forEach { queryTerm ->
                if (keyword == queryTerm) {
                    score += if (keyword in primaryKeywords) (if (primaryKeywords.size == 1) 18 else 12) else 9
                } else if (keyword.startsWith(queryTerm)) {
                    score += if (keyword in primaryKeywords) 6 else 4
                } else if (keyword.length >= 5 && queryTerm.length >= 5 && queryTerm.contains(keyword)) {
                    score += 1
                }
            }
        }
        return score
    }

    companion object {
        private var iconKeywords: Map<String, Set<String>> = emptyMap()
    }
}
