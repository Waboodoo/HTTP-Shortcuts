package ch.rmy.android.http_shortcuts.actions

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object ActionsUtil {

    private const val RAW_ACTION_REGEX = "_runAction\\(\"([a-z0-9_]+)\", (.*)\\); /\\* built-in \\*/" // TODO: Refine this regex

    private val PATTERN = Pattern.compile(RAW_ACTION_REGEX, Pattern.CASE_INSENSITIVE)

    private fun actionToJS(actionDTO: ActionDTO): String =
        "_runAction(\"${actionDTO.type}\", ${GsonUtil.toJson(actionDTO.data)}); /* built-in */"


    fun addSpans(context: Context, script: CharSequence, actionFactory: ActionFactory): Spannable {
        val color = color(context, R.color.action)

        val builder = SpannableStringBuilder(script)
        val matcher = match(script)

        val replacements = LinkedList<Replacement>()
        while (matcher.find()) {
            val actionType = matcher.group(1)
            val payload = GsonUtil.fromJsonObject<String>(matcher.group(2))
            val actionDTO = ActionDTO(actionType, payload)
            replacements.add(Replacement(matcher.start(), matcher.end(), actionDTO))
        }

        val it = replacements.descendingIterator()
        while (it.hasNext()) {
            val replacement = it.next()
            val actionDTO = replacement.actionDTO
            val action = actionFactory.fromDTO(actionDTO)
            val placeholderText = SpannableStringBuilder()
                .apply {
                    append("action(\"")
                    append(action.actionType.title)
                    append("\", ...);")

                }
            val span = ActionSpan(color, actionDTO)
            builder.replace(replacement.startIndex, replacement.endIndex, placeholderText)
            builder.setSpan(span, replacement.startIndex, replacement.startIndex + placeholderText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return builder
    }

    fun removeSpans(text: Spannable): String {
        val builder = StringBuilder(text)
        text.getSpans(0, text.length, ActionSpan::class.java)
            .sortedByDescending {
                text.getSpanStart(it)
            }
            .forEach { span ->
                val start = text.getSpanStart(span)
                val end = text.getSpanEnd(span)
                val replacement = actionToJS(span.actionDTO)
                builder.replace(start, end, replacement)
            }

        return builder.toString()
    }

    private fun match(s: CharSequence): Matcher = PATTERN.matcher(s)

    private class Replacement(internal val startIndex: Int, internal val endIndex: Int, internal val actionDTO: ActionDTO)
}