package ch.rmy.android.http_shortcuts.http

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.webkit.MimeTypeMap
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.utils.FileUtil
import java.io.FileNotFoundException

class FileUploadManager private constructor(
    private val contentResolver: ContentResolver,
    private val sharedFileUris: List<Uri>,
    private val forwardedFileIds: List<String>,
    private val fileRequests: Iterator<FileRequest>,
) {

    private val registeredFiles = mutableListOf<List<File>>()

    init {
        val sharedFiles = sharedFileUris.iterator()
        while (sharedFiles.hasNext()) {
            val request = getNextFileRequest() ?: break
            registerFiles(
                if (request.multiple) {
                    sharedFiles.asSequence().toList().map(::uriToFile)
                } else {
                    listOf(sharedFiles.next().let(::uriToFile))
                }
            )
        }
        val forwardedFiles = forwardedFileIds.iterator()
        while (forwardedFiles.hasNext()) {
            val request = getNextFileRequest() ?: break
            registerFiles(
                if (request.multiple) {
                    forwardedFiles.asSequence().toList().mapNotNull(::getFileById)
                } else {
                    forwardedFiles.next().let(::getFileById)?.let(::listOf) ?: emptyList()
                }
            )
        }
    }

    private fun registerFiles(files: List<File>) {
        registeredFiles.add(files)
        files.associateByTo(fileLookup) { it.id }
    }

    fun getFiles(): List<File> =
        registeredFiles.flatten()

    fun getFiles(index: Int): List<File> =
        registeredFiles.getOrNull(index) ?: emptyList()

    fun getFile(index: Int): File? =
        registeredFiles.getOrNull(index)?.firstOrNull()

    fun getNextFileRequest(): FileRequest? =
        fileRequests.takeIf { it.hasNext() }?.next()

    fun fulfilFileRequest(fileUris: List<Uri>) {
        registerFiles(fileUris.map(::uriToFile))
    }

    private fun getType(file: Uri): String =
        contentResolver.getType(file) ?: FALLBACK_TYPE

    private fun uriToFile(uri: Uri): File =
        getType(uri).let { type ->
            File(
                id = generateId(),
                mimeType = type,
                fileName = getFileName(uri, type),
                data = uri,
                fileSize = getFileSize(uri),
            )
        }

    private fun getFileById(fileId: String): File? =
        fileLookup[fileId]

    private fun getFileName(file: Uri, type: String): String {
        // Case 1: The file was shared into the app and we can look up its original name instead of using the name of the cache file
        val cachedFileName = FileUtil.getCacheFileOriginalName(file)
        if (cachedFileName != null) {
            return cachedFileName
        }

        // Case 2: The content resolver provides the file's name
        tryOrLog {
            val fileName = FileUtil.getFileName(contentResolver, file)
            if (fileName != null) {
                return fileName
            }
        }

        // Case 3: We failed to determine the file name and fall back to a constructed file name, while trying to pick an appropriate file extension
        val potentialFileName = file.lastPathSegment ?: DEFAULT_FILE_NAME
        val expectedExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
        if (expectedExtension != null && !potentialFileName.endsWith(".$expectedExtension", ignoreCase = true)) {
            return "$potentialFileName.$expectedExtension"
        }
        return potentialFileName
    }

    private fun getFileSize(file: Uri): Long? =
        try {
            contentResolver.openAssetFileDescriptor(file, "r")
                ?.length
                ?.takeUnless { it == AssetFileDescriptor.UNKNOWN_LENGTH }
        } catch (e: FileNotFoundException) {
            null
        }

    private fun generateId(): String =
        "file-${newUUID()}"

    data class File(
        val id: String,
        val mimeType: String,
        val fileName: String,
        val data: Uri,
        val fileSize: Long?,
    )

    class FileRequest(val multiple: Boolean, val image: Boolean)

    class Builder(private val contentResolver: ContentResolver) {

        private var sharedFiles: List<Uri>? = null
        private var forwardedFileIds: List<String>? = null
        private val fileRequests = mutableListOf<FileRequest>()

        fun withSharedFiles(fileUris: List<Uri>) = also {
            this.sharedFiles = fileUris
        }

        fun withForwardedFiles(forwardedFileIds: List<String>) = also {
            this.forwardedFileIds = forwardedFileIds
        }

        fun addFileRequest(multiple: Boolean = false, image: Boolean = false) = also {
            fileRequests.add(FileRequest(multiple, image))
        }

        fun build() = FileUploadManager(
            contentResolver = contentResolver,
            sharedFileUris = sharedFiles ?: emptyList(),
            forwardedFileIds = forwardedFileIds ?: emptyList(),
            fileRequests = fileRequests.iterator(),
        )
    }

    companion object {

        private const val FALLBACK_TYPE = "application/octet-stream"
        private const val DEFAULT_FILE_NAME = "file"

        private val fileLookup = mutableMapOf<String, File>()
    }
}
