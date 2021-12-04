package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class InvalidProxyException(val error: String) : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_invalid_proxy, error)
}
