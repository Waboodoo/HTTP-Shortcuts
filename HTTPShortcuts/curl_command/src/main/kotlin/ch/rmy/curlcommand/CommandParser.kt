package ch.rmy.curlcommand

import java.util.StringTokenizer

object CommandParser {

    private enum class State {
        INIT,
        SINGLE_QUOTE,
        DOUBLE_QUOTE,
    }

    fun parseCommand(command: String): List<String> {
        val tokenizer = StringTokenizer("$command ", " '\"\\", true)
        var state = State.INIT
        val arguments = mutableListOf<String>()

        var flush = false
        val builder = StringBuilder()

        var previousToken: String
        var currentToken = ""
        loop@ while (tokenizer.hasMoreTokens()) {
            previousToken = currentToken
            currentToken = tokenizer.nextToken()
            when (state) {
                State.SINGLE_QUOTE -> {
                    if (currentToken == "\'" && previousToken != "\\") {
                        state = State.INIT
                        flush = true
                        continue@loop
                    }
                }
                State.DOUBLE_QUOTE -> {
                    if (currentToken == "\"" && previousToken != "\\") {
                        state = State.INIT
                        flush = true
                        continue@loop
                    }
                }
                State.INIT -> {
                    when (currentToken) {
                        "\'" -> {
                            state = State.SINGLE_QUOTE
                            continue@loop
                        }
                        "\"" -> {
                            state = State.DOUBLE_QUOTE
                            continue@loop
                        }
                        " " -> {
                            if (flush || builder.isNotEmpty()) {
                                arguments.add(builder.toString())
                                builder.setLength(0)
                            }
                            continue@loop
                        }
                    }
                    flush = false
                }
            }
            if (currentToken == "\\" && previousToken != "\\") {
                continue
            }
            builder.append(currentToken)
            if (currentToken == "\\") {
                currentToken = ""
            }
        }
        return arguments
    }
}
