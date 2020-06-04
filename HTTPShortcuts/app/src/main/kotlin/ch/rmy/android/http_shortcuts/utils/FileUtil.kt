package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import ch.rmy.android.http_shortcuts.extensions.tryOrLog
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.Date

object FileUtil {

    private const val MAX_CACHE_FILE_AGE = 5 * 60 * 1000

    fun createCacheFile(context: Context, file: String): Uri =
        getUriFromFile(context, File(context.cacheDir, file))

    fun getWriter(context: Context, uri: Uri) =
        BufferedWriter(
            OutputStreamWriter(context.contentResolver.openOutputStream(uri, "w")!!)
        )

    private fun getUriFromFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

    fun deleteOldCacheFiles(context: Context) {
        tryOrLog {
            val now = Date().time
            context.cacheDir
                .listFiles()
                ?.filter { now - it.lastModified() > MAX_CACHE_FILE_AGE }
                ?.forEach {
                    it.delete()
                }
        }
    }

}