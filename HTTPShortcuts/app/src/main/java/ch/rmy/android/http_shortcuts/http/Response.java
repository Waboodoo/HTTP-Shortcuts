package ch.rmy.android.http_shortcuts.http;

public class Response {

    private final String body;

    Response(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
