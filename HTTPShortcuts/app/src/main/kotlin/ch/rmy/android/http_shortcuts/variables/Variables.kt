package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.utils.UUIDUtils.UUID_REGEX
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.Pattern.quote

object Variables {

    const val KEY_MAX_LENGTH = 30

    const val VARIABLE_KEY_REGEX = "[A-Za-z0-9_]{1,$KEY_MAX_LENGTH}"
    const val VARIABLE_ID_REGEX = "($UUID_REGEX|[0-9]+)"

    private const val RAW_PLACEHOLDER_PREFIX = "{{"
    private const val RAW_PLACEHOLDER_SUFFIX = "}}"
    val RAW_PLACEHOLDER_REGEX = "${quote(RAW_PLACEHOLDER_PREFIX)}$VARIABLE_ID_REGEX${quote(RAW_PLACEHOLDER_SUFFIX)}"
    val BROKEN_RAW_PLACEHOLDER_REGEX = "${quote(RAW_PLACEHOLDER_PREFIX)}$VARIABLE_ID_REGEX\\}(?!\\})"

    private const val JS_PLACEHOLDER_REGEX = """/\*\[variable]\*/"([^"]+)"/\*\[/variable]\*/"""
    private const val JS_PLACEHOLDER_REGEX2 = """getVariable\(["']($VARIABLE_KEY_REGEX)["']\)"""

    private val PATTERN = Pattern.compile(RAW_PLACEHOLDER_REGEX, Pattern.CASE_INSENSITIVE)
    private val JS_PATTERN = Pattern.compile(JS_PLACEHOLDER_REGEX)
    private val JS_PATTERN2 = Pattern.compile(JS_PLACEHOLDER_REGEX2)

    fun isValidVariableKey(variableKey: String) =
        VARIABLE_KEY_REGEX.toRegex().matchEntire(variableKey) != null

    fun rawPlaceholdersToResolvedValues(string: String, variables: Map<VariableId, String>): String {
        val builder = StringBuilder()
        val matcher = match(string)
        var previousEnd = 0
        while (matcher.find()) {
            builder.append(string.substring(previousEnd, matcher.start()))
            val variableId = matcher.group(1)!!
            builder.append(variables[variableId] ?: matcher.group(0))
            previousEnd = matcher.end()
        }
        builder.append(string.substring(previousEnd, string.length))
        return builder.toString()
    }

    /**
     * Searches for variable placeholders and returns all variable IDs found in them.
     */
    internal fun extractVariableIds(string: String): Set<VariableId> =
        buildSet {
            val matcher = match(string)
            while (matcher.find()) {
                add(matcher.group(1)!!)
            }
        }

    private fun match(s: CharSequence): Matcher = PATTERN.matcher(s)

    /**
     * Searches for variable placeholders in JS code and returns all variable IDs found in them.
     */
    internal fun extractVariableIdsFromJS(string: String): Set<VariableId> =
        buildSet {
            val matcher = JS_PATTERN.matcher(string)
            while (matcher.find()) {
                add(matcher.group(1)!!)
            }
        }

    internal fun extractVariableKeysFromJS(string: String): Set<VariableKey> =
        buildSet {
            val matcher = JS_PATTERN2.matcher(string)
            while (matcher.find()) {
                add(matcher.group(1)!!)
            }
        }
}
