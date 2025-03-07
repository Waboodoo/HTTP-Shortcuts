package ch.rmy.android.framework.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import ch.rmy.android.framework.extensions.applyIf
import ch.rmy.android.framework.extensions.minus
import ch.rmy.android.framework.extensions.tryOrLog
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

object FileUtil {

    private val cacheFileNames: MutableMap<Uri, String> = ConcurrentHashMap()
    private val cacheFileTypes: MutableMap<Uri, String> = ConcurrentHashMap()

    fun createCacheFile(context: Context, file: String, deleteIfExists: Boolean = false): Uri =
        getUriFromFile(
            context,
            File(context.cacheDir, file)
                .applyIf(deleteIfExists) {
                    delete()
                },
        )

    fun getCacheFileIfValid(context: Context, file: String): Uri? =
        File(context.cacheDir, file)
            .takeIf { it.isFile && it.length() > 0 }
            ?.let {
                getUriFromFile(context, it)
            }

    fun getOutputStream(context: Context, uri: Uri) =
        context.contentResolver.openOutputStream(uri, "wt")!!

    fun getWriter(context: Context, uri: Uri) =
        BufferedWriter(OutputStreamWriter(getOutputStream(context, uri)))

    fun getUriFromFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file,
        )

    fun deleteOldCacheFiles(context: Context, maxCacheFileAge: Duration) {
        val now = Instant.now()
        context.cacheDir
            .listFiles()
            ?.filter { now - Instant.ofEpochMilli(it.lastModified()) > maxCacheFileAge }
            ?.forEach {
                it.delete()
            }
    }

    fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String? {
        if (fileUri.scheme == "content") {
            tryOrLog {
                contentResolver.query(fileUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        cursor.moveToFirst()
                        val fileName = cursor
                            .takeUnless { it.isAfterLast }
                            ?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            .takeUnless { it == -1 }
                            ?.let(cursor::getString)
                        if (fileName != null) {
                            return fileName
                        }
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

    fun getCacheFileOriginalType(cacheFileUri: Uri): String? =
        cacheFileTypes[cacheFileUri]

    fun putCacheFileOriginalType(cacheFileUri: Uri, name: String) {
        cacheFileTypes[cacheFileUri] = name
    }
}
