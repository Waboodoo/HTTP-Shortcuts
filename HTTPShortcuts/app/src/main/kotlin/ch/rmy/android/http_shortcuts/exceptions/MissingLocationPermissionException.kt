package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class MissingLocationPermissionException : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_missing_location_permission)
}
