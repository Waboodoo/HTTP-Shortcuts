package ch.rmy.curlcommand

import kotlin.test.Test
import kotlin.test.assertEquals

class CurlConstructorTest {

    @Test
    fun testCurlConstructorString() {
        val curlCommand = CurlCommand.Builder()
            .method("POST")
            .data("Hello World")
            .username("user")
            .password("password123")
            .timeout(42)
            .header("Key", "Value")
            .proxy("192.168.1.42", 1337)
            .url("http://example.com")
            .build()

        val expected = "curl http://example.com -X POST -m 42 -u user:password123 -x 192.168.1.42:1337 -H \"Key: Value\" -d \"Hello World\""
        val actual = CurlConstructor.toCurlCommandString(curlCommand)
        assertEquals(expected, actual)
    }

    @Test
    fun testDigestAuth() {
        val curlCommand = CurlCommand.Builder()
            .isDigestAuth()
            .username("user")
            .password("password123")
            .url("http://example.com")
            .build()

        val expected = "curl http://example.com --digest -u user:password123"
        val actual = CurlConstructor.toCurlCommandString(curlCommand)
        assertEquals(expected, actual)
    }

    @Test
    fun `insecure flag`() {
        val curlCommand = CurlCommand.Builder()
            .insecure()
            .url("http://example.com")
            .build()

        val expected = "curl http://example.com --insecure"
        val actual = CurlConstructor.toCurlCommandString(curlCommand)
        assertEquals(expected, actual)
    }

    @Test
    fun `silent flag`() {
        val curlCommand = CurlCommand.Builder()
            .silent()
            .url("http://example.com")
            .build()

        val expected = "curl http://example.com --silent"
        val actual = CurlConstructor.toCurlCommandString(curlCommand)
        assertEquals(expected, actual)
    }

    @Test
    fun testCurlConstructorParsed() {
        val originalCommand = CurlCommand.Builder()
            .method("POST")
            .data("Hello World")
            .username("user")
            .password("password123")
            .timeout(42)
            .header("Key", "Value")
            .header("Key2", "Value2")
            .proxy("192.168.1.42", 1337)
            .insecure()
            .url("http://example.com")
            .build()

        val commandString = CurlConstructor.toCurlCommandString(originalCommand)

        val parsedCommand = CurlParser.parse(commandString)

        assertEquals(originalCommand.url, parsedCommand.url)
        assertEquals(originalCommand.timeout.toLong(), parsedCommand.timeout.toLong())
        assertEquals(originalCommand.method, parsedCommand.method)
        assertEquals(originalCommand.username, parsedCommand.username)
        assertEquals(originalCommand.password, parsedCommand.password)
        assertEquals(originalCommand.data, parsedCommand.data)
        assertEquals(originalCommand.headers, parsedCommand.headers)
        assertEquals(originalCommand.proxyHost, parsedCommand.proxyHost)
        assertEquals(originalCommand.proxyPort, parsedCommand.proxyPort)
        assertEquals(originalCommand.insecure, parsedCommand.insecure)
    }
}
