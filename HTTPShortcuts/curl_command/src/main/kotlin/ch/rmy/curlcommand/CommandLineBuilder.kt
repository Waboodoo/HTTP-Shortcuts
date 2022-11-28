package ch.rmy.curlcommand

import java.util.regex.Matcher

class CommandLineBuilder(private val command: String) {

    private val arguments = mutableListOf<String>()

    fun argument(argument: Any) = also {
        arguments.add(escapeIfNecessary(argument.toString()))
    }

    fun option(option: String, vararg arguments: Any) = also {
        if (!option.matches(OPTION_PATTERN.toRegex())) {
            throw IllegalArgumentException()
        }
        this.arguments.add(option)
        for (argument in arguments) {
            argument(argument)
        }
    }

    fun build(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(command)
        for (argument in arguments) {
            stringBuilder.append(' ')
            stringBuilder.append(argument)
        }
        return stringBuilder.toString()
    }

    companion object {

        private const val OPTION_PATTERN = "^-{1,2}[a-zA-Z0-9][a-zA-Z0-9-_]*$"

        internal fun escapeIfNecessary(string: String): String =
            if (needsEscaping(string)) escape(string) else string

        private fun needsEscaping(string: String): Boolean =
            string.contains("\"") || string.contains(" ") || string.contains("<") || string.contains(">")

        private fun escape(string: String): String =
            "\"" + string.replace("\\\\".toRegex(), Matcher.quoteReplacement("\\")).replace("\"".toRegex(), Matcher.quoteReplacement("\\\"")) + "\""
    }
}
