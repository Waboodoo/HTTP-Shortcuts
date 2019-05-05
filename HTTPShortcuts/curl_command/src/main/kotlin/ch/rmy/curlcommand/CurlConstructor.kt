package ch.rmy.curlcommand

object CurlConstructor {

    fun toCurlCommandString(curlCommand: CurlCommand): String {
        val builder = CommandLineBuilder("curl")

        builder.argument(curlCommand.url)

        if (curlCommand.method != "GET") {
            builder.option("-X", curlCommand.method)
        }

        if (curlCommand.timeout != 0) {
            builder.option("-m", curlCommand.timeout)
        }

        if (curlCommand.username.isNotEmpty()) {
            builder.option("-u", curlCommand.username + if (curlCommand.password.isNotEmpty()) ":" + curlCommand.password else "")
        }

        for ((key, value) in curlCommand.headers) {
            builder.option("-H", "$key: $value")
        }

        if (curlCommand.data.isNotEmpty()) {
            builder.option("-d", curlCommand.data)
        }

        return builder.build()
    }

}
