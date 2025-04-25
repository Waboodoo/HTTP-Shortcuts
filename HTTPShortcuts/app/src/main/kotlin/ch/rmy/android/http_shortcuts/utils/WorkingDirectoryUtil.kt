package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkingDirectoryUtil
@Inject
constructor(
    @ApplicationContext
    private val context: Context,
) {
    fun getDocumentFile(workingDirectory: WorkingDirectory): DocumentFile? =
        getDocumentFile(workingDirectory.directory)

    fun getDocumentFile(workingDirectoryUri: Uri): DocumentFile? =
        try {
            DocumentFile.fromTreeUri(context, workingDirectoryUri)!!
        } catch (_: IllegalArgumentException) {
            null
        }

    fun isMounted(workingDirectoryUri: Uri): Boolean =
        getDocumentFile(workingDirectoryUri)?.isDirectory == true

    fun getFileNames(workingDirectory: WorkingDirectory): List<String>? =
        getDocumentFile(workingDirectory)
            ?.listFiles()
            ?.mapNotNull { it.name }
            ?.sortedWith { name1, name2 -> name1.compareTo(name2, ignoreCase = true) }
}
