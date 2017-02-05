package ch.rmy.curlparser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurlParserTest {

    @Test
    public void testCurlParser() {
        String target = "curl \"https://foo\" -X POST -H \"Authorization: Cookie\" -H \"Accept: application/json\" --data \"This is my \\\"data\\\"\" --compressed";
        CurlCommand command = CurlParser.parse(target);

        assertEquals("https://foo", command.url);
        assertEquals("POST", command.method);
        assertEquals("Cookie", command.headers.get("Authorization"));
        assertEquals("application/json", command.headers.get("Accept"));
        assertEquals(2, command.headers.size());
        assertEquals("This is my \"data\"", command.data);
    }

}
