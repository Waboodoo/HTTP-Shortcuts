package ch.rmy.android.http_shortcuts.http

import android.content.Context
import javax.inject.Inject

class ResponseFileStorageFactory
@Inject
constructor(
    private val context: Context,
) {
    fun create(sessionId: String, storeDirectory: String?): ResponseFileStorage =
        ResponseFileStorage(context, sessionId, storeDirectory)
}
