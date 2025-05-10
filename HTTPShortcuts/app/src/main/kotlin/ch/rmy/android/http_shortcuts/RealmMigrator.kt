package ch.rmy.android.http_shortcuts

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import ch.rmy.android.framework.utils.FileUtil
import java.io.File
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionMethod

object RealmMigrator {
    fun check(context: Context): Boolean {
        val realmFile = getRealmFile(context)
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val version = preferences.getInt(MIGRATION_VERSION_KEY, 0)

        if (version == 4) {
            return true
        }
        if (!realmFile.exists()) {
            preferences.edit {
                putInt(MIGRATION_VERSION_KEY, 4)
            }
            return true
        }
        if (version == 3) {
            realmFile.delete()
            preferences.edit {
                putInt(MIGRATION_VERSION_KEY, 4)
            }
            return true
        }
        return false
    }

    private fun getRealmFile(context: Context): File =
        File(context.filesDir, "shortcuts_db_v2")

    fun deleteRealmFile(context: Context) {
        getRealmFile(context).delete()
        setMigrated(context)
    }

    fun setMigrated(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        preferences.edit {
            putInt(MIGRATION_VERSION_KEY, 4)
        }
        Application.unmigratedRealmFound = false
    }

    fun getShareableRealmExportFile(context: Context): Uri {
        val realmFile = getRealmFile(context)
        val zipFile = File(context.cacheDir, "shortcuts-legacy-export.zip")
        val uri = FileUtil.getUriFromFile(context, zipFile)
        ZipOutputStream(FileUtil.getOutputStream(context, uri)).use { out ->
            val zipParameters = ZipParameters().apply {
                compressionMethod = CompressionMethod.DEFLATE
            }
            zipParameters.fileNameInZip = "shortcuts_db_v2"
            out.putNextEntry(zipParameters)
            realmFile.inputStream().use { it.copyTo(out) }
            out.closeEntry()

            context.filesDir.listFiles { it.name.startsWith("custom-icon_") }
                ?.forEach { iconFile ->
                    val zipParameters = ZipParameters().apply {
                        compressionMethod = CompressionMethod.DEFLATE
                    }
                    zipParameters.fileNameInZip = iconFile.name
                    out.putNextEntry(zipParameters)
                    iconFile.inputStream().use { it.copyTo(out) }
                    out.closeEntry()
                }
        }

        return uri
    }

    private const val PREFERENCES_NAME = "http_shortcuts.realm_to_room_preferences"
    private const val MIGRATION_VERSION_KEY = "migration_version"
}
