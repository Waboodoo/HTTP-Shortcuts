package ch.rmy.android.framework.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import ch.rmy.android.framework.extensions.applyIf
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter

object FileUtil {

    fun createCacheFile(context: Context, file: String, deleteIfExists: Boolean = false): Uri =
        getUriFromFile(
            context,
            File(context.cacheDir, file)
                .applyIf(deleteIfExists) {
                    delete()
                },
        )

    fun deleteCacheFile(context: Context, file: String) {
        File(context.cacheDir, file).delete()
    }

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
}
