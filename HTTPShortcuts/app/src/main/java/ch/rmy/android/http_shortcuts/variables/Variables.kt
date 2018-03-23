package ch.rmy.android.http_shortcuts.variables

import java.util.regex.Matcher
import java.util.regex.Pattern

object Variables {

    private const val VARIABLE_KEY_REGEX = "[A-Za-z0-9]{1,20}"

    private const val RAW_PLACEHOLDER_PREFIX = "{{"
    private const val RAW_PLACEHOLDER_SUFFIX = "}}"
    private const val RAW_PLACEHOLDER_REGEX = "\\{\\{($VARIABLE_KEY_REGEX)\\}\\}"

    private const val PRETTY_PLACEHOLDER_PREFIX = "{"
    private const val PRETTY_PLACEHOLDER_SUFFIX = "}"

    private val PATTERN = Pattern.compile(RAW_PLACEHOLDER_REGEX)

    fun match(s: CharSequence): Matcher = PATTERN.matcher(s)

    fun isValidVariableKey(variableKey: String) =
            VARIABLE_KEY_REGEX.toRegex().matchEntire(variableKey) != null

    fun insert(string: String, variables: ResolvedVariables): String {
        val builder = StringBuilder()
        val matcher = Variables.match(string)
        var previousEnd = 0
        while (matcher.find()) {
            builder.append(string.substring(previousEnd, matcher.start()))
            val variableKey = matcher.group(1)
            builder.append(variables[variableKey] ?: matcher.group(0))
            previousEnd = matcher.end()
        }
        builder.append(string.substring(previousEnd, string.length))
        return builder.toString()
    }

    internal fun extractVariableKeys(string: String): Set<String> {
        val discoveredVariables = mutableSetOf<String>()
        val matcher = match(string)
        while (matcher.find()) {
            discoveredVariables.add(matcher.group(1))
        }
        return discoveredVariables
    }

    fun toRawPlaceholder(variableKey: String) = "$RAW_PLACEHOLDER_PREFIX$variableKey$RAW_PLACEHOLDER_SUFFIX"

    fun toPrettyPlaceholder(variableKey: String) = "$PRETTY_PLACEHOLDER_PREFIX$variableKey$PRETTY_PLACEHOLDER_SUFFIX"

}
