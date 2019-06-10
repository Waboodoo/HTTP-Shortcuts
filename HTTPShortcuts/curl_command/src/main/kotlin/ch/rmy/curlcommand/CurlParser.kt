package ch.rmy.curlcommand

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class CurlParser private constructor(arguments: List<String>) {

    private val builder = CurlCommand.Builder()

    init {
        val args = arguments.toMutableList()
        val iterator = args.listIterator()
        var urlFound = false
        loop@ while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            var argument = iterator.next()

            if (argument.equals("curl", ignoreCase = true) && index == 0) {
                continue
            }

            if (argument.startsWith("-") && !argument.startsWith("--") && argument.length > 2) {
                val argumentParameter = argument.substring(2)
                argument = argument.substring(0, 2)
                iterator.add(argumentParameter)
                iterator.previous()
            }

            // arguments with 1 parameter
            if (iterator.hasNext()) {
                when (argument) {
                    "-X", "--request" -> {
                        builder.method(iterator.next())
                        continue@loop
                    }
                    "-H", "--header" -> {
                        val header = iterator.next().split(": ".toRegex(), 2)
                        if (header.size == 2) {
                            builder.header(header[0], header[1])
                        }
                        continue@loop
                    }
                    "-d", "--data", "--data-binary", "--data-urlencode" -> {
                        var data = iterator.next()
                        if (argument == "--data-urlencode") {
                            try {
                                data = if (data.contains("=")) {
                                    val parts = data.split("=".toRegex(), 2)
                                    parts[0] + "=" + URLEncoder.encode(parts[1], "utf-8")
                                } else {
                                    URLEncoder.encode(data, "utf-8")
                                }
                            } catch (e: UnsupportedEncodingException) {
                            }

                        }
                        builder.data(data)
                        continue@loop
                    }
                    "-m", "--max-time", "--connect-timeout" -> {
                        try {
                            builder.timeout(Integer.parseInt(iterator.next()))
                        } catch (e: NumberFormatException) {
                        }

                        continue@loop
                    }
                    "-u", "--user" -> {
                        val credentials = iterator.next().split(":".toRegex(), 2)
                        builder.username(credentials[0])
                        if (credentials.size > 1) {
                            builder.password(credentials[1])
                        }
                        continue@loop
                    }
                    "-A", "--user-agent" -> {
                        builder.header("User-Agent", iterator.next())
                        continue@loop
                    }
                    "--url" -> {
                        val url = iterator.next()
                        if (url.startsWith("http", ignoreCase = true)) {
                            builder.url(url)
                        } else {
                            builder.url("http://$url")
                        }
                        continue@loop
                    }
                    "-e", "--referer" -> {
                        builder.header("Referer", iterator.next())
                        continue@loop
                    }
                }
            }

            // arguments with 0 parameters
            when (argument) {
                "-G", "--get" -> {
                    builder.method(CurlCommand.METHOD_GET)
                    continue@loop
                }
            }

            if (argument.startsWith("http", ignoreCase = true)) {
                urlFound = true
                builder.url(argument)
            } else if (!argument.startsWith("-") && !urlFound) {
                urlFound = true
                builder.url("http://$argument")
            }
        }
    }

    companion object {

        fun parse(commandString: String): CurlCommand {
            val arguments = CommandParser.parseCommand(commandString)
            val curlParser = CurlParser(arguments)
            return curlParser.builder.build()
        }
    }

}
