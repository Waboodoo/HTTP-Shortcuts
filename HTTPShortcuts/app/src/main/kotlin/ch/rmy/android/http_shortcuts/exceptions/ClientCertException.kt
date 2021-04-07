package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class ClientCertException : UserException() {
    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_failed_to_use_client_cert)
}
