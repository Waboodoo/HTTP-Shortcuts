package ch.rmy.android.http_shortcuts.import_export

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.support.design.widget.Snackbar
import android.view.View

abstract class SimpleTask<T>(protected val context: Context, private val baseView: View) : AsyncTask<T, Unit, Exception?>() {

    private var progressDialog: Dialog? = null

    protected fun getString(resId: Int) = context.getString(resId)!!

    protected abstract val progressMessage: String

    protected abstract val successMessage: String

    protected abstract val failureMessage: String

    override fun onPreExecute() {
        progressDialog = ProgressDialog.show(context, null, progressMessage)
    }

    override fun onPostExecute(exception: Exception?) {
        progressDialog?.dismiss()
        Snackbar.make(baseView, if (exception == null) successMessage else failureMessage, Snackbar.LENGTH_LONG).show()
    }

}
