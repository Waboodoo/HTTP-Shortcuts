package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class UnsupportedFeatureException : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_not_supported)
}
