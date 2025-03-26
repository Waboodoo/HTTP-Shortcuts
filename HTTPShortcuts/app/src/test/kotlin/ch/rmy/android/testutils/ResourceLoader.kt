package ch.rmy.android.testutils

import java.io.InputStream

object ResourceLoader {
    fun getStream(file: String): InputStream =
        javaClass.classLoader!!.getResourceAsStream(file)
}
