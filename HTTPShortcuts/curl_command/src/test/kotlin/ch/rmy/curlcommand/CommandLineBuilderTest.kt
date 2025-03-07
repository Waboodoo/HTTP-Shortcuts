package ch.rmy.curlcommand

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows

class CommandLineBuilderTest {

    @Test
    fun testCommandLineBuilder() {
        val expected = "foo -bla 25 --foo-bar \"Hello \\\"World\\\"\" true -bla Hello \"\\\"World\\\"\""
        val actual = CommandLineBuilder("foo")
            .option("-bla")
            .argument(25)
            .option("--foo-bar")
            .argument("Hello \"World\"")
            .argument(true)
            .option("-bla", "Hello", "\"World\"")
            .build()
        assertEquals(expected, actual)
    }

    @Test
    fun testBackslashes() {
        val expected = "foo \" \\\" \\"
        val actual = CommandLineBuilder("foo")
            .argument(" \\")
            .argument("\\")
            .build()
        assertEquals(expected, actual)
    }

    @Test
    fun testIllegalOption() {
        assertThrows<IllegalArgumentException> {
            CommandLineBuilder("foo")
                .option("invalidOption")
        }
    }
}
