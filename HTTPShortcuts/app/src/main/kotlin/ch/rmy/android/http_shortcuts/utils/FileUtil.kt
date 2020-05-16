package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter

object FileUtil {

    fun createCacheFile(context: Context, file: String): Uri =
        getUriFromFile(context, File(context.cacheDir, file))

    fun getWriter(context: Context, uri: Uri) =
        BufferedWriter(
            OutputStreamWriter(context.contentResolver.openOutputStream(uri, "w")!!)
        )

    fun getUriFromFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

}