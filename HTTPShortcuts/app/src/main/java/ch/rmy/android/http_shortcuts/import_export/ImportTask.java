package ch.rmy.android.http_shortcuts.import_export;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager;

public class ImportTask extends SimpleTask<Uri> {

    public ImportTask(Context context, View baseView) {
        super(context, baseView);
    }

    @Override
    protected Boolean doInBackground(Uri... uris) {
        Uri uri = uris[0];

        Controller controller = null;
        Reader reader = null;
        try {
            try {
                controller = new Controller();
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    return false;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                Base base = GsonUtil.importData(reader);
                ImportMigrator.migrate(base);
                controller.importBase(base);
                LauncherShortcutManager.updateAppShortcuts(getContext(), controller.getCategories());
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (controller != null) {
                    controller.destroy();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected String getProgressMessage() {
        return getString(R.string.import_in_progress);
    }

    @Override
    protected String getSuccessMessage() {
        return getString(R.string.import_success);
    }

    @Override
    protected String getFailureMessage() {
        return getString(R.string.import_failed);
    }

}