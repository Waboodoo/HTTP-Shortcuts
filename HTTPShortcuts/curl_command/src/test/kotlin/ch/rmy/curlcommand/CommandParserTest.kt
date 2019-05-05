package ch.rmy.curlcommand

import org.junit.Assert.assertEquals
import org.junit.Test

class CommandParserTest {

    @Test
    fun testParser() {
        val target = "curl -X \"Hello 'World'\" \"escaped \\\" quotes and backslashes \\\\\" --bla 'foo'"
        val expected = listOf("curl", "-X", "Hello 'World'", "escaped \" quotes and backslashes \\", "--bla", "foo")
        val actual = CommandParser.parseCommand(target)
        assertEquals(expected, actual)
    }

}
