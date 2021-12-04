package ch.rmy.android.http_shortcuts.utils

import java.io.IOException
import java.io.Reader

class SizeLimitedReader(private val reader: Reader, private val maxBytes: Long) : Reader() {

    private var bytesRead: Long = 0

    override fun read(b: CharArray, off: Int, len: Int): Int {
        bytesRead += len
        checkLimit()
        return reader.read(b, off, len)
    }

    override fun close() {
        reader.close()
    }

    private fun checkLimit() {
        if (bytesRead > maxBytes) {
            throw LimitReachedException(maxBytes)
        }
    }

    class LimitReachedException(val limit: Long) : IOException()
}
