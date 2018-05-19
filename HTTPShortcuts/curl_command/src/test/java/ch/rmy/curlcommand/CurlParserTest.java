package ch.rmy.curlcommand;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurlParserTest {

    @Test
    public void testCurlParser() {
        String target = "curl \"https://foo\" -X POST -H \"Authorization: Cookie\" -H \"Accept: application/json\" --data \"This is my \\\"data\\\"\" -u foo:bar --compressed";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("https://foo", command.getUrl());
        assertEquals("POST", command.getMethod());
        assertEquals("Cookie", command.getHeaders().get("Authorization"));
        assertEquals("application/json", command.getHeaders().get("Accept"));
        assertEquals(2, command.getHeaders().size());
        assertEquals("This is my \"data\"", command.getData());
        assertEquals("foo", command.getUsername());
        assertEquals("bar", command.getPassword());
    }

    @Test
    public void testUrlEncodedData() {
        String target = "curl --data-urlencode \"Hä&?4\"";
        CurlCommand command = CurlParser.parse(target);
        assertEquals("H%C3%83%C2%A4%26%3F4", command.getData());
    }

    @Test
    public void testUrlEncodedData2() {
        String target = "curl --data-urlencode \"föö=Hä&?4\"";
        CurlCommand command = CurlParser.parse(target);
        assertEquals("föö=H%C3%83%C2%A4%26%3F4", command.getData());
    }

    @Test
    public void testNoSpaceAfterArgument() {
        String target = "curl -ufoo:bar -XPOST";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("POST", command.getMethod());
        assertEquals("foo", command.getUsername());
        assertEquals("bar", command.getPassword());
    }

    @Test
    public void testMultipleDataArguments() {
        String target = "curl --data Hello -d \" world\"";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("Hello world", command.getData());
    }

    @Test
    public void testMethodChangeWithData() {
        String target = "curl 'https://foo?bar' -X PUT --data-binary '{}' --compressed";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("PUT", command.getMethod());
        assertEquals("https://foo?bar", command.getUrl());
        assertEquals("{}", command.getData());
    }

    @Test
    public void testUserAgent() {
        String target = "curl foo -A 'Custom Agent'";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("Custom Agent", command.getHeaders().get("User-Agent"));
    }

    @Test
    public void testUserAgent2() {
        String target = "curl foo --user-agent 'Custom Agent'";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("Custom Agent", command.getHeaders().get("User-Agent"));
    }

    @Test
    public void testReferer() {
        String target = "curl foo -e 'http://foo'";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("http://foo", command.getHeaders().get("Referer"));
    }

    @Test
    public void testReferer2() {
        String target = "curl foo --referer 'http://foo'";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("http://foo", command.getHeaders().get("Referer"));
    }

}
