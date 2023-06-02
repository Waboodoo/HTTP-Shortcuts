package ch.rmy.android.http_shortcuts.data

import android.net.Uri

sealed class RealmError {
    object RealmNotFound : RealmError()
    data class ConfigurationError(
        val backupFile: Uri,
    ) : RealmError()
}
