package ch.rmy.curlcommand

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurlParserTest {

    @Test
    fun testCurlParser() {
        val target = "curl \"https://foo\" -X POST -H \"Authorization: Cookie\" -H \"Accept: application/json\" --proxy 192.168.1.42:1337 --data \"This is my \\\"data\\\"\" -u foo:bar --compressed"
        val command = CurlParser.parse(target)

        assertEquals("https://foo", command.url)
        assertEquals("POST", command.method)
        assertEquals("Cookie", command.headers["Authorization"])
        assertEquals("application/json", command.headers["Accept"])
        assertEquals(2, command.headers.size)
        assertEquals(listOf("This is my \"data\""), command.data)
        assertEquals("foo", command.username)
        assertEquals("bar", command.password)
        assertEquals("192.168.1.42", command.proxyHost)
        assertEquals(1337, command.proxyPort)
    }

    @Test
    fun testNoScheme() {
        val target = "curl httpbla.com"
        val command = CurlParser.parse(target)
        assertEquals("http://httpbla.com", command.url)
    }

    @Test
    fun testUrlEncodedData() {
        val target = "curl --data-urlencode \"Hä&?4\""
        val command = CurlParser.parse(target)
        assertEquals(listOf("H%C3%A4%26%3F4"), command.data)
        assertFalse(command.isFormData)
    }

    @Test
    fun testUrlEncodedData2() {
        val target = "curl --data-urlencode \"föö=Hä&?4\""
        val command = CurlParser.parse(target)
        assertEquals(listOf("föö=H%C3%A4%26%3F4"), command.data)
        assertFalse(command.isFormData)
    }

    @Test
    fun testFormData() {
        val target = "curl -F foo=bar"
        val command = CurlParser.parse(target)
        assertEquals(listOf("foo=bar"), command.data)
        assertTrue(command.isFormData)
    }

    @Test
    fun testMultipleFormData() {
        val target = "curl -F foo=bar --form file=@somefile.txt"
        val command = CurlParser.parse(target)
        assertEquals(listOf("foo=bar", "file=@somefile.txt"), command.data)
        assertTrue(command.isFormData)
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

        assertEquals(listOf("Hello", " world"), command.data)
    }

    @Test
    fun testMethodChangeWithData() {
        val target = "curl 'https://foo?bar' -X PUT --data-binary '{}' --compressed"
        val command = CurlParser.parse(target)

        assertEquals("PUT", command.method)
        assertEquals("https://foo?bar", command.url)
        assertEquals(listOf("{}"), command.data)
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

    @Test
    fun testProxy() {
        val target = "curl foo -x 'foo:42'"
        val command = CurlParser.parse(target)

        assertEquals("foo", command.proxyHost)
        assertEquals(42, command.proxyPort)
    }

}
