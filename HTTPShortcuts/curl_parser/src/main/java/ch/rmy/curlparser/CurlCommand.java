package ch.rmy.curlparser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CurlCommand implements Serializable {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    String url = "";
    String method = METHOD_GET;
    Map<String, String> headers = new HashMap<>();
    String data = "";
    int timeout = 0;
    String username = "";
    String password = "";

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getData() {
        return data;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
