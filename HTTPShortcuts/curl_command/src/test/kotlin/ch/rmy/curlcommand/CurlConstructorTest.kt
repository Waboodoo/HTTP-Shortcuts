package ch.rmy.curlcommand

import org.junit.Assert.assertEquals
import org.junit.Test

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
            .url("http://example.com")
            .build()

        val expected = "curl http://example.com -X POST -m 42 -u user:password123 -H \"Key: Value\" -d \"Hello World\""
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
    }

}
