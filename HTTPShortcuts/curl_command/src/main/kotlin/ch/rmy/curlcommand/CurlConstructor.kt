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
            if (curlCommand.isDigestAuth) {
                builder.option("--digest")
            }
            builder.option("-u", curlCommand.username + if (curlCommand.password.isNotEmpty()) ":" + curlCommand.password else "")
        }

        if (curlCommand.proxyHost.isNotEmpty()) {
            builder.option("-x", "${curlCommand.proxyHost}:${curlCommand.proxyPort}")
        }

        for ((key, value) in curlCommand.headers) {
            builder.option("-H", "$key: $value")
        }

        curlCommand.data.forEach { data ->
            if (curlCommand.isFormData) {
                builder.option("-F", data)
            } else {
                builder.option("-d", data)
            }
        }

        if (curlCommand.usesBinaryData) {
            builder.option("--data-binary", "@file")
        }

        if (curlCommand.insecure) {
            builder.option("--insecure")
        }

        if (curlCommand.silent) {
            builder.option("--silent")
        }

        if (curlCommand.ipVersion4) {
            builder.option("--ipv4")
        }

        if (curlCommand.ipVersion6) {
            builder.option("--ipv6")
        }

        return builder.build()
    }
}
