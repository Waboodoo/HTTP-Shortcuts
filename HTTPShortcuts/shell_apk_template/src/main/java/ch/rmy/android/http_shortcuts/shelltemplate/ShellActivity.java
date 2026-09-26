package ch.rmy.android.http_shortcuts.shelltemplate;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class ShellActivity extends Activity {

    private static final String META_DATA_TARGET_URI = "target_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String targetUri = getTargetUri();
        if (targetUri == null || targetUri.isEmpty()) {
            showShortcutNotFoundMessage();
            finish();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUri));
            String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
            String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (title != null) {
                intent.putExtra(Intent.EXTRA_TITLE, title);
            }
            if (text != null) {
                intent.putExtra(Intent.EXTRA_TEXT, text);
            }
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            showShortcutNotFoundMessage();
        } finally {
            finish();
        }
    }

    private String getTargetUri() {
        try {
            ActivityInfo info = getPackageManager()
                .getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            if (info.metaData == null) {
                return null;
            }
            return info.metaData.getString(META_DATA_TARGET_URI);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void showShortcutNotFoundMessage() {
        Toast.makeText(this, R.string.message_shell_apk_shortcut_not_found, Toast.LENGTH_LONG).show();
    }
}
