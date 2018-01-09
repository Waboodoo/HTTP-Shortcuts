package ch.rmy.android.http_shortcuts.variables

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object Variables {

    internal const val PREFIX_LENGTH = 2
    internal const val SUFFIX_LENGTH = 2

    private const val VARIABLE_NAME_REGEX = "[A-Za-z0-9]+"
    private const val FULL_VARIABLE_NAME_REGEX = "^$VARIABLE_NAME_REGEX$"
    private const val PLACEHOLDER_REGEX = "\\{\\{$VARIABLE_NAME_REGEX\\}\\}"

    private val PATTERN = Pattern.compile(PLACEHOLDER_REGEX)

    fun match(s: CharSequence): Matcher = PATTERN.matcher(s)

    fun isValidVariableName(variableName: String) =
            variableName.matches(FULL_VARIABLE_NAME_REGEX.toRegex())

    fun insert(string: String, variables: ResolvedVariables): String {
        val builder = StringBuilder()
        val matcher = Variables.match(string)
        var previousEnd = 0
        while (matcher.find()) {
            builder.append(string.substring(previousEnd, matcher.start()))
            val placeholder = string.substring(matcher.start(), matcher.end())
            val variableName = string.substring(matcher.start() + PREFIX_LENGTH, matcher.end() - SUFFIX_LENGTH)
            builder.append(if (variables.hasValue(variableName)) variables.getValue(variableName) else placeholder)
            previousEnd = matcher.end()
        }
        builder.append(string.substring(previousEnd, string.length))
        return builder.toString()
    }

    internal fun extractVariableNames(string: String): Set<String> {
        val discoveredVariables = HashSet<String>()
        val matcher = match(string)
        while (matcher.find()) {
            val variableName = string.substring(matcher.start() + PREFIX_LENGTH, matcher.end() - SUFFIX_LENGTH)
            discoveredVariables.add(variableName)
        }
        return discoveredVariables
    }

}
