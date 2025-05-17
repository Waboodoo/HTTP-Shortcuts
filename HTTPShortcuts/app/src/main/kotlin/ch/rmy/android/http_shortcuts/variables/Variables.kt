package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.utils.UUIDUtils.UUID_REGEX
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.Pattern.quote

object Variables {

    const val KEY_MAX_LENGTH = 30
    const val VARIABLE_KEY_REGEX = "[A-Za-z0-9_]{1,$KEY_MAX_LENGTH}"
    private const val VARIABLE_PLACEHOLDER_INNER_REGEX = "($UUID_REGEX|[0-9]+|$VARIABLE_KEY_REGEX)"

    private const val RAW_PLACEHOLDER_PREFIX = "{{"
    private const val RAW_PLACEHOLDER_SUFFIX = "}}"
    val RAW_PLACEHOLDER_REGEX = "${quote(RAW_PLACEHOLDER_PREFIX)}$VARIABLE_PLACEHOLDER_INNER_REGEX${quote(RAW_PLACEHOLDER_SUFFIX)}"
    val BROKEN_RAW_PLACEHOLDER_REGEX = "${quote(RAW_PLACEHOLDER_PREFIX)}($UUID_REGEX)\\}(?!\\})"

    private const val JS_PLACEHOLDER_REGEX = """/\*\[variable]\*/"([^"]+)"/\*\[/variable]\*/"""
    private const val JS_PLACEHOLDER_REGEX2 = """getVariable\(["']($VARIABLE_KEY_REGEX)["']\)"""

    private val PATTERN = Pattern.compile(RAW_PLACEHOLDER_REGEX, Pattern.CASE_INSENSITIVE)
    private val JS_PATTERN = Pattern.compile(JS_PLACEHOLDER_REGEX)
    private val JS_PATTERN2 = Pattern.compile(JS_PLACEHOLDER_REGEX2)

    fun isValidVariableKey(variableKey: String) =
        VARIABLE_KEY_REGEX.toRegex().matchEntire(variableKey) != null

    fun coerceToVariableKey(variableKey: String) =
        variableKey.replace("[^A-Za-z0-9_]".toRegex(), "").take(KEY_MAX_LENGTH)

    fun isValidGlobalVariableId(variableId: String) =
        variableId.isUUID() || variableId.isInt()

    fun rawPlaceholdersToResolvedValues(string: String, resolvedValues: ResolvedVariableValues): String {
        val builder = StringBuilder()
        val matcher = match(string)
        var previousEnd = 0
        while (matcher.find()) {
            builder.append(string.substring(previousEnd, matcher.start()))
            val variableKeyOrId = VariableKeyOrId(matcher.group(1)!!)
            builder.append(resolvedValues[variableKeyOrId] ?: matcher.group(0))
            previousEnd = matcher.end()
        }
        builder.append(string.substring(previousEnd, string.length))
        return builder.toString()
    }

    internal fun findResolvableVariableIdentifiers(string: String): Set<VariableKeyOrId> =
        buildSet {
            val matcher = match(string)
            while (matcher.find()) {
                add(VariableKeyOrId(matcher.group(1)!!))
            }
        }

    private fun match(s: CharSequence): Matcher = PATTERN.matcher(s)

    internal fun findResolvableVariableIdentifiersFromJS(string: String): Set<VariableKeyOrId> =
        buildSet {
            val matcher = JS_PATTERN.matcher(string)
            while (matcher.find()) {
                add(VariableKeyOrId(matcher.group(1)!!))
            }

            val matcher2 = JS_PATTERN2.matcher(string)
            while (matcher2.find()) {
                add(VariableKeyOrId(matcher2.group(1)!!))
            }
        }
}
