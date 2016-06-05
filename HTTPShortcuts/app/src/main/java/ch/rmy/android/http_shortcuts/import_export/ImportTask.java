package ch.rmy.android.http_shortcuts.import_export;

import android.content.Context;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;

public class ImportTask extends SimpleTask {

    public ImportTask(Context context, View baseView) {
        super(context, baseView);
    }

    @Override
    protected Boolean doInBackground(String... path) {
        String filePath = path[0];

        Controller controller = null;
        Reader reader = null;
        try {
            try {
                controller = new Controller(getContext());
                reader = new BufferedReader(new FileReader(filePath));
                Base base = GsonUtil.importData(reader);
                controller.importBase(base);
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