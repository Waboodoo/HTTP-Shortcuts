package ch.rmy.curlcommand;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurlConstructorTest {

    @Test
    public void testCurlConstructorString() {
        CurlCommand curlCommand = new CurlCommand.Builder()
                .method("POST")
                .data("Hello World")
                .username("user")
                .password("password123")
                .timeout(42)
                .header("Key", "Value")
                .url("http://example.com")
                .build();

        String expected = "curl http://example.com -X POST -m 42 -u user:password123 -H \"Key: Value\" -d \"Hello World\"";
        String actual = CurlConstructor.toCurlCommandString(curlCommand);
        assertEquals(expected, actual);
    }

    @Test
    public void testCurlConstructorParsed() {
        CurlCommand originalCommand = new CurlCommand.Builder()
                .method("POST")
                .data("Hello World")
                .username("user")
                .password("password123")
                .timeout(42)
                .header("Key", "Value")
                .header("Key2", "Value2")
                .url("http://example.com")
                .build();

        String commandString = CurlConstructor.toCurlCommandString(originalCommand);

        CurlCommand parsedCommand = CurlParser.parse(commandString);

        assertEquals(originalCommand.getUrl(), parsedCommand.getUrl());
        assertEquals(originalCommand.getTimeout(), parsedCommand.getTimeout());
        assertEquals(originalCommand.getMethod(), parsedCommand.getMethod());
        assertEquals(originalCommand.getUsername(), parsedCommand.getUsername());
        assertEquals(originalCommand.getPassword(), parsedCommand.getPassword());
        assertEquals(originalCommand.getData(), parsedCommand.getData());
        assertEquals(originalCommand.getHeaders(), parsedCommand.getHeaders());
    }

}
