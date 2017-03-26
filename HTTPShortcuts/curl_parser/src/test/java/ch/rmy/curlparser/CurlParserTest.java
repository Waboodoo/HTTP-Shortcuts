package ch.rmy.curlparser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurlParserTest {

    @Test
    public void testCurlParser() {
        String target = "curl \"https://foo\" -X POST -H \"Authorization: Cookie\" -H \"Accept: application/json\" --data \"This is my \\\"data\\\"\" -u foo:bar --compressed";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("https://foo", command.url);
        assertEquals("POST", command.method);
        assertEquals("Cookie", command.headers.get("Authorization"));
        assertEquals("application/json", command.headers.get("Accept"));
        assertEquals(2, command.headers.size());
        assertEquals("This is my \"data\"", command.data);
        assertEquals("foo", command.username);
        assertEquals("bar", command.password);
    }

    @Test
    public void testUrlEncodedData() {
        String target = "curl --data-urlencode \"Hä&?4\"";
        CurlCommand command = CurlParser.parse(target);
        assertEquals("H%C3%83%C2%A4%26%3F4", command.data);
    }

    @Test
    public void testUrlEncodedData2() {
        String target = "curl --data-urlencode \"föö=Hä&?4\"";
        CurlCommand command = CurlParser.parse(target);
        assertEquals("föö=H%C3%83%C2%A4%26%3F4", command.data);
    }

    @Test
    public void testNoSpaceAfterArgument() {
        String target = "curl -ufoo:bar -XPOST";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("POST", command.method);
        assertEquals("foo", command.username);
        assertEquals("bar", command.password);
    }

    @Test
    public void testMultipleDataArguments() {
        String target = "curl --data Hello -d \" world\"";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("Hello world", command.data);
    }

    @Test
    public void testMethodChangeWithData() {
        String target = "curl 'https://foo?bar' -X PUT --data-binary '{}' --compressed";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("PUT", command.method);
        assertEquals("https://foo?bar", command.url);
        assertEquals("{}", command.data);
    }

}
