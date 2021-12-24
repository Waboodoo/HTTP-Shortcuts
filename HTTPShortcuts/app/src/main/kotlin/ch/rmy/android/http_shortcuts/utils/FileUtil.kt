package ch.rmy.android.http_shortcuts.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import ch.rmy.android.framework.extensions.tryOrLog
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

object FileUtil {

    private const val MAX_CACHE_FILE_AGE = 5 * 60 * 1000

    private val cacheFileNames: MutableMap<Uri, String> = ConcurrentHashMap()

    fun createCacheFile(context: Context, file: String): Uri =
        getUriFromFile(context, File(context.cacheDir, file))

    fun getOutputStream(context: Context, uri: Uri) =
        context.contentResolver.openOutputStream(uri, "w")!!

    fun getWriter(context: Context, uri: Uri) =
        BufferedWriter(OutputStreamWriter(getOutputStream(context, uri)))

    fun getUriFromFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file,
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

    fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String? {
        if (fileUri.scheme == "content") {
            contentResolver.query(fileUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    cursor.moveToFirst()
                    val fileName = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        .takeUnless { it == -1 }
                        ?.let(cursor::getString)
                    if (fileName != null) {
                        return fileName
                    }
                }
        }
        return null
    }

    fun getCacheFileOriginalName(cacheFileUri: Uri): String? =
        cacheFileNames[cacheFileUri]

    fun putCacheFileOriginalName(cacheFileUri: Uri, name: String) {
        cacheFileNames[cacheFileUri] = name
    }
}
