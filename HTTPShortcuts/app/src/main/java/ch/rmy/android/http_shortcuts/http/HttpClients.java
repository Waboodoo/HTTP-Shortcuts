package ch.rmy.android.http_shortcuts.http;

import com.squareup.okhttp.OkHttpClient;

public class HttpClients {

    public static OkHttpClient getDefaultOkHttpClient() {
        return new OkHttpClient();
    }

}
