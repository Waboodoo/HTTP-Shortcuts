package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class Connectivity {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }

}
