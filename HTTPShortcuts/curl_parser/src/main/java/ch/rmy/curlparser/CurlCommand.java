package ch.rmy.curlparser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CurlCommand implements Serializable {

    String url = "";
    String method = "GET";
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
