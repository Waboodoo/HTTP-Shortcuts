package ch.rmy.android.http_shortcuts.import_export;

import android.content.Context;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;

public class ExportTask extends SimpleTask<String> {

    private static final String FILE_NAME = "shortcuts";
    private static final String FILE_EXTENSION = ".json";

    public ExportTask(Context context, View baseView) {
        super(context, baseView);
    }

    @Override
    protected Boolean doInBackground(String... path) {
        Controller controller = new Controller(getContext());
        Base base = controller.exportBase();
        controller.destroy();

        File file = getFile(path[0]);
        Writer writer = null;
        try {
            try {
                writer = new BufferedWriter(new FileWriter(file));
                GsonUtil.exportData(base, writer);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private File getFile(String directoryPath) {
        File directory = new File(directoryPath);

        File file = new File(directory, FILE_NAME + FILE_EXTENSION);
        int count = 2;
        while (file.exists()) {
            file = new File(directory, FILE_NAME + count + FILE_EXTENSION);
            count++;
        }
        return file;
    }

    @Override
    protected String getProgressMessage() {
        return getString(R.string.export_in_progress);
    }

    @Override
    protected String getSuccessMessage() {
        return getString(R.string.export_success);
    }

    @Override
    protected String getFailureMessage() {
        return getString(R.string.export_failed);
    }

}
