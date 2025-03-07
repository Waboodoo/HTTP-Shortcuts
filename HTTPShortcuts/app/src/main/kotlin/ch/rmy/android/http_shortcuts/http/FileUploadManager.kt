package ch.rmy.android.http_shortcuts.http

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.exifinterface.media.ExifInterface
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import java.io.FileNotFoundException
import java.io.IOException

class FileUploadManager internal constructor(
    private val contentResolver: ContentResolver,
    private val fileRequests: Iterator<FileRequest>,
    private val withMetaData: Boolean,
    private val transformation: suspend (FileRequest, Uri, mimeType: String) -> Uri?,
) {

    private val registeredFiles = mutableListOf<List<File>>()

    suspend fun registerSharedFiles(sharedFileUris: List<Uri>) {
        val sharedFiles = sharedFileUris.iterator()
        while (sharedFiles.hasNext()) {
            val request = getNextFileRequest() ?: break
            registerFiles(
                request,
                if (request.multiple) {
                    sharedFiles.asSequence().toList().map(::uriToFile)
                } else {
                    listOf(sharedFiles.next().let(::uriToFile))
                },
            )
        }
    }

    suspend fun registerForwardedFiles(forwardedFileIds: List<String>) {
        val forwardedFiles = forwardedFileIds.iterator()
        while (forwardedFiles.hasNext()) {
            val request = getNextFileRequest() ?: break
            registerFiles(
                request,
                if (request.multiple) {
                    forwardedFiles.asSequence().toList().mapNotNull(::getFileById)
                } else {
                    forwardedFiles.next().let(::getFileById)?.let(::listOf) ?: emptyList()
                },
            )
        }
    }

    private suspend fun registerFiles(fileRequest: FileRequest, files: List<File>) {
        val transformedFiles = files.map { file ->
            transformation(fileRequest, file.data, file.mimeType)
                ?.let { newUri ->
                    file.copy(
                        data = newUri,
                        fileSize = getFileSize(newUri),
                    )
                }
                ?: file
        }
        registeredFiles.add(transformedFiles)
        transformedFiles.associateByTo(fileLookup) { it.id }
    }

    fun getNextFileRequest(): FileRequest? =
        fileRequests.takeIf { it.hasNext() }?.next()

    suspend fun fulfillFileRequest(fileRequest: FileRequest, fileUris: List<Uri>) {
        registerFiles(
            fileRequest,
            fileUris.map(::uriToFile),
        )
    }

    private fun getType(file: Uri): String {
        val cachedFileType = FileUtil.getCacheFileOriginalType(file)
        if (cachedFileType != null && cachedFileType != FALLBACK_TYPE) {
            return cachedFileType
        }
        return contentResolver.getType(file) ?: FALLBACK_TYPE
    }

    private fun uriToFile(uri: Uri): File =
        getType(uri).let { type ->
            File(
                id = generateId(),
                mimeType = type,
                fileName = getFileName(uri, type),
                data = uri,
                fileSize = getFileSize(uri),
                metaData = if (withMetaData) getMetaData(uri, type) else null,
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
                ?.use {
                    it.length
                }
                ?.takeUnless { it == AssetFileDescriptor.UNKNOWN_LENGTH }
        } catch (e: FileNotFoundException) {
            null
        }

    private fun getMetaData(file: Uri, type: String): FileMetaData? {
        if (!type.startsWith("image/", ignoreCase = true)) {
            return null
        }
        return try {
            contentResolver.openInputStream(file)!!
                .use(::ExifInterface)
                .let { exifInterface ->
                    FileMetaData(
                        orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0),
                        created = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)?.formatDateTime(),
                    )
                }
        } catch (_: IOException) {
            null
        }
    }

    private fun String.formatDateTime(): String? =
        takeIf { it.firstOrNull()?.isDigit() == true }
            ?.replaceFirst(':', '-')
            ?.replaceFirst(':', '-')

    private fun generateId(): String =
        newUUID()

    fun getResult(): Result =
        Result(registeredFiles)

    data class File(
        val id: String,
        val mimeType: String,
        val fileName: String,
        val data: Uri,
        val fileSize: Long?,
        val metaData: FileMetaData?,
    )

    data class FileMetaData(
        val orientation: Int,
        val created: String?,
    )

    data class Result(
        private val registeredFiles: List<List<File>>,
    ) {

        fun getFiles(): List<File> =
            registeredFiles.flatten()

        fun getFiles(index: Int): List<File> =
            registeredFiles.getOrNull(index) ?: emptyList()

        fun getFile(index: Int): File? =
            registeredFiles.getOrNull(index)?.firstOrNull()
    }

    class FileRequest(
        val multiple: Boolean,
        val fromCamera: Boolean,
        val fromFile: Uri?,
        val withImageEditor: Boolean,
    )

    class Builder(private val contentResolver: ContentResolver) {

        private val fileRequests = mutableListOf<FileRequest>()
        private var withMetaData = false
        private var transformation: suspend (FileRequest, Uri, mimeType: String) -> Uri? = { _, _, _ -> null }

        fun addFileRequest(
            multiple: Boolean = false,
            fromCamera: Boolean = false,
            fromFile: Uri? = null,
            withImageEditor: Boolean = false,
        ) = also {
            fileRequests.add(FileRequest(multiple, fromCamera, fromFile, withImageEditor))
        }

        fun withMetaData(enabled: Boolean) = also {
            withMetaData = enabled
        }

        fun withTransformation(transformation: suspend (FileRequest, Uri, mimeType: String) -> Uri?) = also {
            this.transformation = transformation
        }

        fun build() = FileUploadManager(
            contentResolver = contentResolver,
            fileRequests = fileRequests.iterator(),
            withMetaData = withMetaData,
            transformation = transformation,
        )
    }

    companion object {

        private const val FALLBACK_TYPE = "application/octet-stream"
        private const val DEFAULT_FILE_NAME = "file"

        private val fileLookup = mutableMapOf<String, File>()
    }
}
