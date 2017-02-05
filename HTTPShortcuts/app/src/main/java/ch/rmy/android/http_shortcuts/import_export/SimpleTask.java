package ch.rmy.android.http_shortcuts.import_export;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

public abstract class SimpleTask<T> extends AsyncTask<T, Void, Boolean> {

    private final Context context;
    private final View baseView;

    private Dialog progressDialog;

    public SimpleTask(Context context, View baseView) {
        this.context = context;
        this.baseView = baseView;
    }

    protected final Context getContext() {
        return context;
    }

    protected final String getString(int resId) {
        return context.getString(resId);
    }

    abstract protected String getProgressMessage();

    abstract protected String getSuccessMessage();

    abstract protected String getFailureMessage();

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, null, getProgressMessage());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        progressDialog.dismiss();
        Snackbar.make(baseView, success ? getSuccessMessage() : getFailureMessage(), Snackbar.LENGTH_LONG).show();
    }

}
