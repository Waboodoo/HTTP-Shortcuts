package ch.rmy.curlcommand;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CurlCommand implements Serializable {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private String url = "";
    private String method = METHOD_GET;
    private Map<String, String> headers = new HashMap<>();
    private String data = "";
    private int timeout = 0;
    private String username = "";
    private String password = "";

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

    public static class Builder {

        private final CurlCommand curlCommand = new CurlCommand();

        public Builder url(String url) {
            curlCommand.url = url;
            return this;
        }

        public Builder method(String method) {
            curlCommand.method = method;
            return this;
        }

        public Builder data(String data) {
            curlCommand.data = curlCommand.data + data;
            if (CurlCommand.METHOD_GET.equals(curlCommand.method)) {
                curlCommand.method = CurlCommand.METHOD_POST;
            }
            return this;
        }

        public Builder timeout(int timeout) {
            curlCommand.timeout = timeout;
            return this;
        }

        public Builder username(String username) {
            curlCommand.username = username;
            return this;
        }

        public Builder password(String password) {
            curlCommand.password = password;
            return this;
        }

        public Builder header(String key, String value) {
            curlCommand.headers.put(key, value);
            return this;
        }

        public CurlCommand build() {
            return curlCommand;
        }

    }

}
