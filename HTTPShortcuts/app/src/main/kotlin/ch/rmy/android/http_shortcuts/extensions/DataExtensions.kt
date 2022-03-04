package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.utils.SizeLimitedReader
import java.io.BufferedReader
import java.io.InputStreamReader

private const val BUFFER_SIZE = 16384

fun Uri.readIntoString(context: Context, limit: Long): String =
    InputStreamReader(context.contentResolver.openInputStream(this))
        .use { reader ->
            BufferedReader(SizeLimitedReader(reader, limit), BUFFER_SIZE)
                .use(BufferedReader::readText)
        }
