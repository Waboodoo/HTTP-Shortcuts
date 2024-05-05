package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.SearchUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IconFilterProvider(private val context: Context) {

    private var iconKeywords: Map<String, Set<String>> = emptyMap()
    private var synonyms: Map<String, Set<String>> = emptyMap()

    suspend fun init(icons: List<ShortcutIcon.BuiltInIcon>) {
        logInfo("Initializing IconFilterProvider")
        tryOrLog {
            val iconIndex = withContext(Dispatchers.IO) {
                GsonUtil.gson.fromJson(
                    context.assets.open("icons_index.json").bufferedReader(),
                    IconIndex::class.java,
                )
            }

            synonyms = buildMap {
                iconIndex.synonyms.forEach { synonymSet ->
                    synonymSet.forEach { word ->
                        put(word, synonymSet.minus(word))
                    }
                }
            }

            iconKeywords = buildMap {
                icons.forEach { icon ->
                    var iconName = icon.normalizedIconName
                    Icons.PREFIXES.forEach { prefix ->
                        iconName = iconName.removePrefix(prefix)
                    }
                    put(icon.normalizedIconName, SearchUtil.normalizeToKeywords(iconName))
                }

                iconIndex.icons.forEach { (iconName, additionalKeywords) ->
                    val keywords = getOrDefault(iconName, emptySet())
                    put(iconName, keywords + additionalKeywords)
                }

                forEach { (icon, keywords) ->
                    put(
                        icon,
                        keywords.flatMap {
                            (synonyms[it] ?: emptySet()) + it
                        }
                            .toSet()
                    )
                }
            }

            logInfo("IconFilterProvider initialized")
        }
    }

    fun createFilter(query: String): ((ShortcutIcon.BuiltInIcon) -> Boolean)? {
        val queryTerms = query.trim().takeUnlessEmpty()?.let { SearchUtil.normalizeToKeywords(it, minLength = 1) }
            ?.flatMap {
                (synonyms[it] ?: emptySet()) + it
            }
            ?: return null
        return { icon ->
            icon.matches(queryTerms)
        }
    }

    private fun ShortcutIcon.BuiltInIcon.matches(queryTerms: Collection<String>): Boolean {
        val keywords = iconKeywords[normalizedIconName]
            ?: emptySet()
        return keywords.any { keyword ->
            queryTerms.any { queryTerm ->
                keyword.contains(queryTerm) || (keyword.length >= 4 && queryTerm.length >= 4 && queryTerm.contains(keyword))
            }
        }
    }
}
