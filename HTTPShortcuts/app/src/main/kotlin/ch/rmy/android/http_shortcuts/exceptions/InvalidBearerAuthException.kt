package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class InvalidBearerAuthException(val token: String) : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_invalid_bearer_auth, token)
}
