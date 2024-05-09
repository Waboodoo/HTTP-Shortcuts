package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.SearchUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.GZIPInputStream

class IconFilterProvider(private val context: Context) {

    private var iconKeywords: Map<String, Set<String>> = emptyMap()

    suspend fun init() {
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

    fun createFilter(query: String): ((ShortcutIcon.BuiltInIcon) -> Boolean)? {
        val queryTerms = query.trim()
            .takeUnlessEmpty()
            ?.let { SearchUtil.normalizeToKeywords(it, minLength = 1) }
            ?: return null
        return { icon ->
            icon.matches(queryTerms)
        }
    }

    private fun ShortcutIcon.BuiltInIcon.matches(queryTerms: Collection<String>): Boolean {
        val keywords = iconKeywords[plainName.filter { it != '_' }]
            ?: SearchUtil.normalizeToKeywords(plainName, minLength = 1)
        return keywords.any { keyword ->
            queryTerms.any { queryTerm ->
                keyword.startsWith(queryTerm) || (keyword.length >= 5 && queryTerm.length >= 5 && queryTerm.contains(keyword))
            }
        }
    }
}
