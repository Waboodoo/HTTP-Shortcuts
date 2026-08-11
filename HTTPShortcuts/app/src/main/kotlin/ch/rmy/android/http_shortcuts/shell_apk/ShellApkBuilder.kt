package ch.rmy.android.http_shortcuts.shell_apk

import android.content.Context
import android.content.pm.PackageManager
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import java.io.File
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShellApkBuilder
@Inject
constructor(
    private val context: Context,
    private val binaryXmlStringPoolEditor: BinaryXmlStringPoolEditor,
    private val shellIconWriter: ShellIconWriter,
    private val shellApkSigner: ShellApkSigner,
) {
    suspend fun build(
        shortcutId: ShortcutId,
        appName: String,
        icon: ShortcutIcon,
    ): File =
        withContext(Dispatchers.IO) {
            val packageName = createPackageName(shortcutId)
            val workDir = File(context.cacheDir, "shell-apk/$packageName")
            workDir.deleteRecursively()
            workDir.mkdirs()

            val unsignedApk = File(workDir, "unsigned.apk")
            val signedApk = File(workDir, "signed.apk")
            val targetUri = "http-shortcuts://$shortcutId"
            val iconBytes = shellIconWriter.createIconPng(icon)

            context.assets.open(TEMPLATE_ASSET).use { input ->
                // The template APK is a tiny, unsigned Android app bundled as an asset. Customizing it here keeps the
                // shell apps independent of Gradle and avoids shipping source-generation tooling on the device.
                rewriteTemplateApk(
                    templateBytes = input.readBytes(),
                    outputFile = unsignedApk,
                    packageName = packageName,
                    appName = appName.ifEmpty { "-" },
                    targetUri = targetUri,
                    iconBytes = iconBytes,
                )
            }
            shellApkSigner.sign(unsignedApk, signedApk)
            validateApk(signedApk)
            signedApk
        }

    private fun createPackageName(shortcutId: ShortcutId): String =
        "$PACKAGE_PREFIX${shortcutId.normalizedForPackageName()}"

    private fun ShortcutId.normalizedForPackageName(): String =
        lowercase(Locale.US)
            .filter(Char::isLetterOrDigit)

    private fun rewriteTemplateApk(
        templateBytes: ByteArray,
        outputFile: File,
        packageName: String,
        appName: String,
        targetUri: String,
        iconBytes: ByteArray,
    ) {
        val entries = buildList {
            var iconWritten = false
            ZipInputStream(templateBytes.inputStream()).use { zipInput ->
                while (true) {
                    val entry = zipInput.nextEntry ?: break
                    if (entry.name.startsWith("META-INF")) {
                        continue
                    }

                    val newBytes = when {
                        entry.name == MANIFEST_ENTRY -> {
                            // AndroidManifest.xml is binary XML inside the APK, so placeholder values are replaced in
                            // its string pool instead of trying to parse it as text.
                            binaryXmlStringPoolEditor.replaceStrings(
                                zipInput.readBytes(),
                                mapOf(
                                    TEMPLATE_PACKAGE_NAME to packageName,
                                    TEMPLATE_APP_NAME to appName,
                                    TEMPLATE_TARGET_URI to targetUri,
                                ),
                            )
                        }
                        entry.name.isTemplateIconEntry() -> {
                            // Resource shrinking/optimization gives launcher PNGs short generated names such as
                            // res/En.png, so the runtime replacement targets all template PNG resources.
                            iconWritten = true
                            iconBytes
                        }
                        else -> zipInput.readBytes()
                    }
                    add(
                        TemplateEntry(
                            name = entry.name,
                            bytes = newBytes,
                            method = entry.method.takeIf { it == ZipEntry.STORED } ?: ZipEntry.DEFLATED,
                            time = entry.time,
                        ),
                    )
                }
            }
            if (!iconWritten) {
                add(
                    TemplateEntry(
                        name = FALLBACK_ICON_ENTRY,
                        bytes = iconBytes,
                        method = ZipEntry.DEFLATED,
                        time = 0,
                    ),
                )
            }
        }

        outputFile.outputStream().use { fileOutput ->
            ZipOutputStream(fileOutput).use { zipOutput ->
                entries
                    .sortedBy { if (it.name == RESOURCES_ENTRY) 0 else 1 }
                    .forEach { entry ->
                        zipOutput.writeEntry(entry)
                    }
            }
        }
    }

    private fun String.isTemplateIconEntry(): Boolean =
        startsWith("res/") && endsWith(".png")

    private fun ZipOutputStream.writeEntry(entry: TemplateEntry) {
        val zipEntry = ZipEntry(entry.name).also { zipEntry ->
            zipEntry.time = entry.time
            if (entry.method == ZipEntry.STORED) {
                // STORED entries must have their size and CRC set before writing, otherwise Android may reject the APK
                // even though the ZIP can still be read by lenient desktop tooling.
                val crc = CRC32().apply {
                    update(entry.bytes)
                }
                zipEntry.method = ZipEntry.STORED
                zipEntry.size = entry.bytes.size.toLong()
                zipEntry.compressedSize = entry.bytes.size.toLong()
                zipEntry.crc = crc.value
            }
        }
        putNextEntry(zipEntry)
        write(entry.bytes)
        closeEntry()
    }

    @Suppress("DEPRECATION")
    private fun validateApk(apkFile: File) {
        context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_ACTIVITIES)
            ?: throw InvalidShellApkException()
    }

    private class TemplateEntry(
        val name: String,
        val bytes: ByteArray,
        val method: Int,
        val time: Long,
    )

    companion object {
        private const val TEMPLATE_ASSET = "shell-apk-template.apk"
        private const val MANIFEST_ENTRY = "AndroidManifest.xml"
        private const val RESOURCES_ENTRY = "resources.arsc"
        private const val FALLBACK_ICON_ENTRY = "res/drawable/ic_launcher_shell.png"
        private const val PACKAGE_PREFIX = "ch.rmy.android.http_shortcuts.app_"
        private const val TEMPLATE_PACKAGE_NAME = "ch.rmy.android.http_shortcuts.shelltemplate"
        private const val TEMPLATE_APP_NAME = "HTTP Shortcuts Shell"
        private const val TEMPLATE_TARGET_URI = "http-shortcuts://shell-template-placeholder"
    }
}
