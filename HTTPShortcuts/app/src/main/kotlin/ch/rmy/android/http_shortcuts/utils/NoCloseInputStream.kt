package ch.rmy.android.http_shortcuts.utils

import java.io.FilterInputStream
import java.io.InputStream

class NoCloseInputStream(inputStream: InputStream) : FilterInputStream(inputStream) {
    override fun close() {
    }
}
