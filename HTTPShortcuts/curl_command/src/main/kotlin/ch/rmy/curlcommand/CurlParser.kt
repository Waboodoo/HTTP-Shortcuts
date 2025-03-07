package ch.rmy.curlcommand

import java.net.URLEncoder

class CurlParser private constructor(arguments: List<String>) {

    private val builder = CurlCommand.Builder()

    init {
        val args = arguments.toMutableList()
        val iterator = args.listIterator()
        var urlFound = false
        var wasNoParamArgument = false
        var spaceInjected = false
        loop@ while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            var argument = iterator.next()

            if (argument.equals("curl", ignoreCase = true) && index == 0) {
                continue
            }

            if (wasNoParamArgument && spaceInjected) {
                argument = "-$argument"
            }

            if (argument.startsWith("-") && !argument.startsWith("--") && argument.length > 2) {
                val argumentParameter = argument.substring(2)
                argument = argument.substring(0, 2)
                spaceInjected = true
                iterator.add(argumentParameter)
                iterator.previous()
            } else {
                spaceInjected = false
            }

            wasNoParamArgument = false

            // arguments with 1 parameter
            if (iterator.hasNext()) {
                when (argument) {
                    "-X", "--request" -> {
                        builder.method(iterator.next())
                        continue@loop
                    }
                    "-x", "--proxy" -> {
                        val parts = iterator.next().split(":")
                        builder.proxy(parts[0], parts.getOrNull(1)?.toIntOrNull() ?: 3128)
                        continue@loop
                    }
                    "-H", "--header" -> {
                        val header = iterator.next().split(":", limit = 2)
                        if (header.size == 2) {
                            builder.header(header[0], header[1].removePrefix(" "))
                        }
                        continue@loop
                    }
                    "-d", "--data", "--data-binary", "--data-urlencode", "--data-raw" -> {
                        builder.methodIfNotYetSet("POST")
                        var dataItems = iterator.next().split("&")
                        if (argument == "--data-urlencode") {
                            dataItems = dataItems.map { data ->
                                if (data.contains("=")) {
                                    val parts = data.split("=", limit = 2)
                                    parts[0] + "=" + URLEncoder.encode(parts[1], "utf-8")
                                } else {
                                    URLEncoder.encode(data, "utf-8")
                                }
                            }
                        }
                        if (argument == "--data-binary" && dataItems.any { it.startsWith("@") }) {
                            builder.usesBinaryData()
                        } else {
                            dataItems.forEach { data ->
                                builder.data(data)
                            }
                        }
                        continue@loop
                    }
                    "-F", "--form" -> {
                        val data = iterator.next()
                        builder.isFormData()
                        builder.data(data)
                        builder.methodIfNotYetSet("POST")
                        continue@loop
                    }
                    "-m", "--max-time", "--connect-timeout" -> {
                        iterator.next()
                            .toIntOrNull()
                            ?.let {
                                builder.timeout(it)
                            }
                        continue@loop
                    }
                    "-u", "--user" -> {
                        val credentials = iterator.next().split(":", limit = 2)
                        builder.username(credentials[0])
                        if (credentials.size > 1) {
                            builder.password(credentials[1])
                        }
                        continue@loop
                    }
                    "--digest" -> {
                        builder.isDigestAuth()
                        continue@loop
                    }
                    "-A", "--user-agent" -> {
                        builder.header("User-Agent", iterator.next())
                        continue@loop
                    }
                    "--url" -> {
                        builder.url(iterator.next())
                        continue@loop
                    }
                    "-e", "--referer" -> {
                        builder.header("Referer", iterator.next())
                        continue@loop
                    }
                }
            }

            // arguments with 0 parameters
            wasNoParamArgument = argument.startsWith("-") && !argument.startsWith("--")
            when (argument) {
                "-G", "--get" -> {
                    builder.forceGet()
                    continue@loop
                }
                "-k", "--insecure" -> {
                    builder.insecure()
                    continue@loop
                }
                "-s", "--silent" -> {
                    builder.silent()
                    continue@loop
                }
                "-I", "--head" -> {
                    builder.method("HEAD")
                    continue@loop
                }
                "-4", "--ipv4" -> {
                    builder.ipVersion4()
                    continue@loop
                }
                "-6", "--ipv6" -> {
                    builder.ipVersion6()
                    continue@loop
                }
            }
            wasNoParamArgument = false

            if (argument.startsWith("http:", ignoreCase = true) || argument.startsWith("https:", ignoreCase = true)) {
                urlFound = true
                builder.url(argument)
            } else if (!argument.startsWith("-") && !urlFound) {
                urlFound = true
                builder.url(argument)
            }
        }
    }

    companion object {

        fun parse(commandString: String): CurlCommand {
            val arguments = CommandParser.parseCommand(commandString)
            val curlParser = CurlParser(arguments)
            return curlParser.builder.build()
        }

        fun isSupportedOption(option: String) =
            option in SUPPORTED_OPTIONS

        private val SUPPORTED_OPTIONS = setOf(
            "-X",
            "--request",
            "-x",
            "--proxy",
            "-H",
            "--header",
            "-d",
            "--data",
            "--data-binary",
            "--data-urlencode",
            "--data-raw",
            "--digest",
            "-F",
            "--form",
            "-m",
            "--max-time",
            "--connect-timeout",
            "-u",
            "--user",
            "-A",
            "--user-agent",
            "--url",
            "-e",
            "--referer",
            "-G",
            "--get",
            "-k",
            "--insecure",
            "-s",
            "--silent",
            "--head",
            "-I",
            "-4",
            "--ipv4",
            "-6",
            "--ipv6",
        )
    }
}
