package ch.rmy.curlcommand

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandParserTest {

    @Test
    fun testParser() {
        val target = "curl -X \"Hello 'World'\" \"escaped \\\" quotes and backslashes \\\\\" --bla 'foo'"
        val expected = listOf("curl", "-X", "Hello 'World'", "escaped \" quotes and backslashes \\", "--bla", "foo")
        val actual = CommandParser.parseCommand(target)
        assertEquals(expected, actual)
    }
}
