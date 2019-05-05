package ch.rmy.curlcommand

import org.junit.Assert.assertEquals
import org.junit.Test

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

    @Test(expected = IllegalArgumentException::class)
    fun testIllegalOption() {
        CommandLineBuilder("foo")
            .option("invalidOption")
    }

}
