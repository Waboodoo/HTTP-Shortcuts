package ch.rmy.curlcommand

import org.junit.Assert.assertEquals
import org.junit.Test

class CurlParserTest {

    @Test
    fun testCurlParser() {
        val target = "curl \"https://foo\" -X POST -H \"Authorization: Cookie\" -H \"Accept: application/json\" --data \"This is my \\\"data\\\"\" -u foo:bar --compressed"
        val command = CurlParser.parse(target)

        assertEquals("https://foo", command.url)
        assertEquals("POST", command.method)
        assertEquals("Cookie", command.headers["Authorization"])
        assertEquals("application/json", command.headers["Accept"])
        assertEquals(2, command.headers.size)
        assertEquals("This is my \"data\"", command.data)
        assertEquals("foo", command.username)
        assertEquals("bar", command.password)
    }

    @Test
    fun testUrlEncodedData() {
        val target = "curl --data-urlencode \"Hä&?4\""
        val command = CurlParser.parse(target)
        assertEquals("H%C3%A4%26%3F4", command.data)
    }

    @Test
    fun testUrlEncodedData2() {
        val target = "curl --data-urlencode \"föö=Hä&?4\""
        val command = CurlParser.parse(target)
        assertEquals("föö=H%C3%A4%26%3F4", command.data)
    }

    @Test
    fun testNoSpaceAfterArgument() {
        val target = "curl -ufoo:bar -XPOST"
        val command = CurlParser.parse(target)

        assertEquals("POST", command.method)
        assertEquals("foo", command.username)
        assertEquals("bar", command.password)
    }

    @Test
    fun testMultipleDataArguments() {
        val target = "curl --data Hello -d \" world\""
        val command = CurlParser.parse(target)

        assertEquals("Hello world", command.data)
    }

    @Test
    fun testMethodChangeWithData() {
        val target = "curl 'https://foo?bar' -X PUT --data-binary '{}' --compressed"
        val command = CurlParser.parse(target)

        assertEquals("PUT", command.method)
        assertEquals("https://foo?bar", command.url)
        assertEquals("{}", command.data)
    }

    @Test
    fun testUserAgent() {
        val target = "curl foo -A 'Custom Agent'"
        val command = CurlParser.parse(target)

        assertEquals("Custom Agent", command.headers["User-Agent"])
    }

    @Test
    fun testUserAgent2() {
        val target = "curl foo --user-agent 'Custom Agent'"
        val command = CurlParser.parse(target)

        assertEquals("Custom Agent", command.headers["User-Agent"])
    }

    @Test
    fun testReferer() {
        val target = "curl foo -e 'http://foo'"
        val command = CurlParser.parse(target)

        assertEquals("http://foo", command.headers["Referer"])
    }

    @Test
    fun testReferer2() {
        val target = "curl foo --referer 'http://foo'"
        val command = CurlParser.parse(target)

        assertEquals("http://foo", command.headers["Referer"])
    }

}
