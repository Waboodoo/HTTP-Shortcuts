package ch.rmy.curlcommand;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandLineBuilderTest {

    @Test
    public void testCommandLineBuilder() {
        String expected = "foo -bla 25 --foo-bar \"Hello \\\"World\\\"\" true -bla Hello \"\\\"World\\\"\"";
        String actual = new CommandLineBuilder("foo")
                .option("-bla")
                .argument(25)
                .option("--foo-bar")
                .argument("Hello \"World\"")
                .argument(true)
                .option("-bla", "Hello", "\"World\"")
                .build();
        assertEquals(expected, actual);
    }

    @Test
    public void testBackslashes() {
        String expected = "foo \" \\\" \\";
        String actual = new CommandLineBuilder("foo")
                .argument(" \\")
                .argument("\\")
                .build();
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalOption() {
        new CommandLineBuilder("foo")
                .option("invalidOption");
    }

}
