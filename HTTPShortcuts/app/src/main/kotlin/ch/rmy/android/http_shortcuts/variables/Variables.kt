package ch.rmy.android.http_shortcuts.variables

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.annotation.ColorInt
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object Variables {

    const val KEY_MAX_LENGTH = 30

    const val VARIABLE_KEY_REGEX = "[A-Za-z0-9_]{1,$KEY_MAX_LENGTH}"
    private const val VARIABLE_ID_REGEX = "([0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}|[0-9]+)"

    private const val RAW_PLACEHOLDER_PREFIX = "{{"
    private const val RAW_PLACEHOLDER_SUFFIX = "}}"
    private const val RAW_PLACEHOLDER_REGEX = "\\{\\{($VARIABLE_ID_REGEX)\\}\\}"

    private const val JS_PLACEHOLDER_REGEX = """/\*\[variable]\*/"([^"]+)"/\*\[/variable]\*/"""
    private const val JS_PLACEHOLDER_REGEX2 = """getVariable\("($VARIABLE_KEY_REGEX)"\)"""

    private const val PRETTY_PLACEHOLDER_PREFIX = "{"
    private const val PRETTY_PLACEHOLDER_SUFFIX = "}"

    private val PATTERN = Pattern.compile(RAW_PLACEHOLDER_REGEX, Pattern.CASE_INSENSITIVE)
    private val JS_PATTERN = Pattern.compile(JS_PLACEHOLDER_REGEX)
    private val JS_PATTERN2 = Pattern.compile(JS_PLACEHOLDER_REGEX2)

    fun isValidVariableKey(variableKey: String) =
        VARIABLE_KEY_REGEX.toRegex().matchEntire(variableKey) != null

    fun rawPlaceholdersToResolvedValues(string: String, variables: Map<String, String>): String {
        val builder = StringBuilder()
        val matcher = match(string)
        var previousEnd = 0
        while (matcher.find()) {
            builder.append(string.substring(previousEnd, matcher.start()))
            val variableId = matcher.group(1)
            builder.append(variables[variableId] ?: matcher.group(0))
            previousEnd = matcher.end()
        }
        builder.append(string.substring(previousEnd, string.length))
        return builder.toString()
    }

    /**
     * Searches for variable placeholders and returns all variable IDs found in them.
     */
    internal fun extractVariableIds(string: String): Set<String> {
        val discoveredVariables = mutableSetOf<String>()
        val matcher = match(string)
        while (matcher.find()) {
            discoveredVariables.add(matcher.group(1))
        }
        return discoveredVariables
    }

    private fun match(s: CharSequence): Matcher = PATTERN.matcher(s)

    fun rawPlaceholdersToVariableSpans(text: CharSequence, variablePlaceholderProvider: VariablePlaceholderProvider, @ColorInt color: Int): Spannable {
        val builder = SpannableStringBuilder(text)
        val matcher = match(text)

        val replacements = LinkedList<Replacement>()
        while (matcher.find()) {
            val variableId = matcher.group(1)
            val placeholder = variablePlaceholderProvider.findPlaceholderById(variableId)
            if (placeholder != null) {
                replacements.add(Replacement(matcher.start(), matcher.end(), placeholder))
            }
        }

        val it = replacements.descendingIterator()
        while (it.hasNext()) {
            val replacement = it.next()
            val placeholderText = toPrettyPlaceholder(replacement.placeholder.variableKey)
            val span = VariableSpan(color, replacement.placeholder.variableId)
            builder.replace(replacement.startIndex, replacement.endIndex, placeholderText)
            builder.setSpan(span, replacement.startIndex, replacement.startIndex + placeholderText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return builder
    }

    fun variableSpansToRawPlaceholders(text: Spannable): String {
        val builder = StringBuilder(text)
        text.getSpans(0, text.length, VariableSpan::class.java)
            .sortedByDescending {
                text.getSpanStart(it)
            }
            .forEach { span ->
                val start = text.getSpanStart(span)
                val end = text.getSpanEnd(span)
                val replacement = toRawPlaceholder(span.variableId)
                builder.replace(start, end, replacement)
            }

        return builder.toString()
    }

    fun applyVariableFormattingToJS(text: Spannable, variablePlaceholderProvider: VariablePlaceholderProvider, @ColorInt color: Int) {
        text.getSpans(0, text.length, JSVariableSpan::class.java)
            .forEach { span ->
                text.removeSpan(span)
            }
        val matcher = JS_PATTERN.matcher(text)
        while (matcher.find()) {
            val variableId = matcher.group(1)
            val placeholder = variablePlaceholderProvider.findPlaceholderById(variableId)
            val variableKey = placeholder?.variableKey ?: "???"
            text.setSpan(JSVariableSpan(color, variableKey), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Searches for variable placeholders in JS code and returns all variable IDs found in them.
     */
    internal fun extractVariableIdsFromJS(string: String): Set<String> {
        val discoveredVariables = mutableSetOf<String>()
        val matcher = JS_PATTERN.matcher(string)
        while (matcher.find()) {
            discoveredVariables.add(matcher.group(1))
        }
        return discoveredVariables
    }

    internal fun extractVariableKeysFromJS(string: String): Set<String> {
        val discoveredVariables = mutableSetOf<String>()
        val matcher = JS_PATTERN2.matcher(string)
        while (matcher.find()) {
            discoveredVariables.add(matcher.group(1))
        }
        return discoveredVariables
    }

    private fun toRawPlaceholder(variableId: String) = "$RAW_PLACEHOLDER_PREFIX$variableId$RAW_PLACEHOLDER_SUFFIX"

    fun toPrettyPlaceholder(variableKey: String) = "$PRETTY_PLACEHOLDER_PREFIX$variableKey$PRETTY_PLACEHOLDER_SUFFIX"

    private class Replacement(internal val startIndex: Int, internal val endIndex: Int, internal val placeholder: VariablePlaceholder)

}
