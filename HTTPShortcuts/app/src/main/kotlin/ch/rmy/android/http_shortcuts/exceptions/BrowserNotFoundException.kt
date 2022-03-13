package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class BrowserNotFoundException(private val packageName: String) : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_browser_not_found, packageName)
}
