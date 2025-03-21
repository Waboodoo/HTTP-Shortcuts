package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.scripting.JsObject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException
import javax.inject.Inject

class GetDirectoryAction
@Inject
constructor(
    @ApplicationContext
    private val context: Context,
    private val workingDirectoryRepository: WorkingDirectoryRepository,
) : Action<GetDirectoryAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): JsObject {
        val workingDirectory = try {
            workingDirectoryRepository.getWorkingDirectoryByNameOrId(directoryNameOrId)
        } catch (_: NoSuchElementException) {
            throw ActionException {
                "Directory \"${directoryNameOrId}\" not found"
            }
        }
        workingDirectoryRepository.touchWorkingDirectory(workingDirectory.id)
        val directory = DocumentFile.fromTreeUri(context, workingDirectory.directory)!!
        if (!directory.isDirectory) {
            throw ActionException {
                "Directory \"${workingDirectory.name}\" is not mounted"
            }
        }

        val contentResolver = context.contentResolver
        return executionContext.scriptingEngine.buildJsObject {
            function("readFile") { args ->
                logInfo("directory.readFile() called")
                val filePath = args.getString(0)!!
                val encoding = args.getString(1)
                val file = directory.findFileFromPath(filePath)
                    ?: executionContext.throwException(
                        ActionException {
                            "File \"$filePath\" not found in directory \"${workingDirectory.name}\""
                        },
                    )
                val charset = encoding?.let {
                    try {
                        Charset.forName(it)
                    } catch (_: IllegalCharsetNameException) {
                        executionContext.throwException(
                            ActionException {
                                "Invalid charset: $it"
                            },
                        )
                    } catch (_: UnsupportedCharsetException) {
                        executionContext.throwException(
                            ActionException {
                                "Unsupported charset: $it"
                            },
                        )
                    }
                } ?: Charsets.UTF_8
                contentResolver.openInputStream(file.uri)!!
                    .use {
                        it.reader(charset).readText()
                    }
            }
            function("writeFile") { args ->
                logInfo("directory.writeFile() called")
                val filePath = args.getString(0)!!
                val content = args.getByteArray(1) ?: return@function null
                val file = directory.findOrCreateFileFromPath(filePath)
                    ?: executionContext.throwException(
                        ActionException {
                            "File \"$filePath\" not found in directory \"${workingDirectory.name}\""
                        },
                    )
                contentResolver.openOutputStream(file.uri, "wt")!!
                    .use { out ->
                        out.write(content)
                    }
            }
            function("appendFile") { args ->
                logInfo("directory.appendFile() called")
                val filePath = args.getString(0)!!
                val content = args.getByteArray(1) ?: return@function null
                val file = directory.findOrCreateFileFromPath(filePath)
                    ?: executionContext.throwException(
                        ActionException {
                            "File \"$filePath\" not found in directory \"${workingDirectory.name}\""
                        },
                    )
                contentResolver.openOutputStream(file.uri, "wa")!!
                    .use { out ->
                        out.write(content)
                    }
            }
        }
    }

    private fun DocumentFile.findFileFromPath(filePath: String): DocumentFile? {
        var fileHandle: DocumentFile = this
        filePath.split('/').forEach { fileName ->
            fileHandle = fileHandle.findFile(fileName)
                ?: return null
        }
        return fileHandle
    }

    private fun DocumentFile.findOrCreateFileFromPath(filePath: String): DocumentFile? {
        var fileHandle: DocumentFile = this
        val parts = filePath.split('/')
        parts.forEachIndexed { index, fileName ->
            if (fileName == "." || fileName == "..") {
                return null
            }
            fileHandle = fileHandle.findFile(fileName)
                ?: (
                    if (index != parts.lastIndex) {
                        fileHandle.createDirectory(fileName)
                    } else {
                        fileHandle.createFile(
                            determineMimeType(fileName),
                            fileName,
                        )
                    }
                    )
                ?: return null
        }
        return fileHandle
    }

    private fun determineMimeType(fileName: String): String =
        File(fileName).extension.takeUnlessEmpty()?.let { extension ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
            ?: "text/plain"

    data class Params(
        val directoryNameOrId: String,
    )
}
