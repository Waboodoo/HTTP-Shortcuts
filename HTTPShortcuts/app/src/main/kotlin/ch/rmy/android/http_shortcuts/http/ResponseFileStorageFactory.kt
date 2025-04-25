package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.utils.WorkingDirectoryUtil
import javax.inject.Inject

class ResponseFileStorageFactory
@Inject
constructor(
    private val context: Context,
    private val workingDirectoryUtil: WorkingDirectoryUtil,
) {
    fun create(sessionId: String, storeDirectoryUri: Uri? = null): ResponseFileStorage =
        ResponseFileStorage(context, workingDirectoryUtil, sessionId, storeDirectoryUri)
}
