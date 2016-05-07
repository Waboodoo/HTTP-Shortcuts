package ch.rmy.android.http_shortcuts.import_export;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.GsonUtil;
import ch.rmy.android.http_shortcuts.utils.ProgressDialog;

public class ExportTask extends AsyncTask<Void, Void, Boolean> {

    private static final String FILE_NAME = "shortcuts";
    private static final String FILE_EXTENSION = ".json";

    private final Context context;
    private final Object data;
    private final String directoryPath;

    private Dialog progressDialog;

    public ExportTask(Context context, Object data, String directoryPath) {
        this.context = context;
        this.data = data;
        this.directoryPath = directoryPath;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, R.string.export_in_progress);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File file = getFile();
        Writer writer = null;
        try {
            try {
                writer = new BufferedWriter(new FileWriter(file));
                GsonUtil.export(data, writer);
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

    private File getFile() {
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
    protected void onPostExecute(Boolean success) {
        progressDialog.dismiss();

        new MaterialDialog.Builder(context)
                .positiveText(R.string.button_ok)
                .content(success ? R.string.export_success : R.string.export_failed)
                .show();
    }
}
