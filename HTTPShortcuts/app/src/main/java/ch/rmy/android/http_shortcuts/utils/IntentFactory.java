package ch.rmy.android.http_shortcuts.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import ch.rmy.android.http_shortcuts.ExecuteActivity;

public class IntentFactory {

    public static Intent createIntent(Context context, long shortcutId) {
        Intent intent = new Intent(context, ExecuteActivity.class);
        intent.setAction(ExecuteActivity.ACTION_EXECUTE_SHORTCUT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        Uri uri = ContentUris.withAppendedId(Uri.fromParts("content", context.getPackageName(), null), shortcutId);
        intent.setData(uri);
        return intent;
    }

}
