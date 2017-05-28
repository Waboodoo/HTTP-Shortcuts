package ch.rmy.android.http_shortcuts.utils;

import java.util.UUID;

public class UUIDUtils {

    public static String create() {
        return UUID.randomUUID().toString();
    }

}
