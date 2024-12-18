package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import javax.inject.Inject

class ResponseFileStorageFactory
@Inject
constructor(
    private val context: Context,
) {
    fun create(sessionId: String, storeDirectoryUri: Uri? = null): ResponseFileStorage =
        ResponseFileStorage(context, sessionId, storeDirectoryUri)
}
