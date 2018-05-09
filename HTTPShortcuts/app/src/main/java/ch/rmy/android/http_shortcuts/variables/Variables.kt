package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import ch.rmy.android.http_shortcuts.realm.Controller
import java.util.*
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

    fun isValidVariableKey(variableKey: String) =
            VARIABLE_KEY_REGEX.toRegex().matchEntire(variableKey) != null

    fun rawPlaceholdersToResolvedValues(string: String, variables: Map<String, String>): String {
        val builder = StringBuilder()
        val matcher = match(string)
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

    private fun match(s: CharSequence): Matcher = PATTERN.matcher(s)

    fun rawPlaceholdersToVariableSpans(text: CharSequence, variablePlaceholderProvider: VariablePlaceholderProvider): Spannable {
        val builder = SpannableStringBuilder(text)
        val matcher = match(text)

        val replacements = LinkedList<Replacement>()
        while (matcher.find()) {
            val variableKey = matcher.group(1)
            val placeholder = variablePlaceholderProvider.findPlaceholder(variableKey)
            if (placeholder != null) {
                replacements.add(Replacement(matcher.start(), matcher.end(), placeholder))
            }
        }

        val it = replacements.descendingIterator()
        while (it.hasNext()) {
            val replacement = it.next()
            val placeholderText = toPrettyPlaceholder(replacement.placeholder.variableKey)
            val span = VariableSpan(replacement.placeholder.color, replacement.placeholder.variableKey)
            builder.replace(replacement.startIndex, replacement.endIndex, placeholderText)
            builder.setSpan(span, replacement.startIndex, replacement.startIndex + placeholderText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return builder
    }

    fun rawPlaceholdersToVariableSpans(context: Context, text: CharSequence): Spannable {
        Controller().use { controller ->
            val variableKeyProvider = VariablePlaceholderProvider(context, controller.getVariables())
            return Variables.rawPlaceholdersToVariableSpans(text, variableKeyProvider)
        }
    }

    fun variableSpansToRawPlaceholders(text: Spannable): String {
        val builder = StringBuilder(text)
        text.getSpans(0, text.length, VariableSpan::class.java)
                .sortedBy {
                    text.getSpanStart(it)
                }
                .reversed()
                .forEach { span ->
                    val start = text.getSpanStart(span)
                    val end = text.getSpanEnd(span)
                    val replacement = toRawPlaceholder(span.variableKey)
                    builder.replace(start, end, replacement)
                }

        return builder.toString()
    }

    fun insertVariableSpan(text: Editable, placeholder: VariablePlaceholder, position: Int) {
        val placeholderText = toPrettyPlaceholder(placeholder.variableKey)
        val span = VariableSpan(placeholder.color, placeholder.variableKey)
        text.insert(position, placeholderText)
        text.setSpan(span, position, position + placeholderText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun toRawPlaceholder(variableKey: String) = "$RAW_PLACEHOLDER_PREFIX$variableKey$RAW_PLACEHOLDER_SUFFIX"

    private fun toPrettyPlaceholder(variableKey: String) = "$PRETTY_PLACEHOLDER_PREFIX$variableKey$PRETTY_PLACEHOLDER_SUFFIX"

    private class Replacement(internal val startIndex: Int, internal val endIndex: Int, internal val placeholder: VariablePlaceholder)

}
