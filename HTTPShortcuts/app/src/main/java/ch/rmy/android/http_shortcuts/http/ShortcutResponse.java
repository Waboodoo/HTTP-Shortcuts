package ch.rmy.android.http_shortcuts.http;

import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class ShortcutResponse {

    public static final String TYPE_TEXT = "text/plain";
    public static final String TYPE_XML = "text/xml";
    public static final String TYPE_JSON = "application/json";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final int statusCode;
    private final Map<String, String> headers;
    private final byte[] data;

    ShortcutResponse(int statusCode, Map<String, String> headers, byte[] data) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.data = data;
    }

    public String getBodyAsString() {
        try {
            return new String(data, HttpHeaderParser.parseCharset(headers));
        } catch (UnsupportedEncodingException var4) {
            return new String(data);
        }
    }

    public String getContentType() {
        if (headers.containsKey(HEADER_CONTENT_TYPE)) {
            return headers.get(HEADER_CONTENT_TYPE).split(";")[0].toLowerCase();
        }
        return TYPE_TEXT;
    }

}
