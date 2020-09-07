package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.migration.ImportMigrator
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.isWebUrl
import ch.rmy.android.http_shortcuts.extensions.logInfo
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.RxUtils
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URISyntaxException
import java.net.URL

class Importer(private val context: Context) {

    fun import(uri: Uri): Single<ImportStatus> =
        RxUtils
            .single {
                val inputStream = getStream(context, uri)
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val importData = JsonParser.parseReader(reader)
                    logInfo("Importing from v${importData.asJsonObject.get("version") ?: "?"}: ${importData.asJsonObject.keySet()}")
                    val migratedImportData = ImportMigrator.migrate(importData)
                    val newBase = GsonUtil.importData(migratedImportData)
                    newBase.validate()
                    importBase(newBase)
                    ImportStatus(
                        importedShortcuts = newBase.shortcuts.size,
                        needsRussianWarning = newBase.shortcuts.any { it.url.contains(".beeline.ru") }
                    )
                }
            }
            .onErrorResumeNext { error ->
                Single.error(handleError(error))
            }
            .subscribeOn(Schedulers.io())

    private fun getStream(context: Context, uri: Uri): InputStream =
        if (uri.isWebUrl) {
            URL(uri.toString()).openStream()
        } else {
            context.contentResolver.openInputStream(uri)
                ?: throw IOException("Failed to open input stream")
        }

    private fun importBase(base: Base) {
        RealmFactory.withRealm { realm ->
            realm.executeTransaction {
                val oldBase = Repository.getBase(realm)!!
                if (base.title != null) {
                    oldBase.title = base.title
                }

                if (oldBase.categories.singleOrNull()?.shortcuts?.isEmpty() == true) {
                    oldBase.categories.clear()
                }

                base.categories.forEach { category ->
                    importCategory(realm, oldBase, category)
                }

                val persistedVariables = realm.copyToRealmOrUpdate(base.variables)
                oldBase.variables.removeAll(persistedVariables)
                oldBase.variables.addAll(persistedVariables)
            }
        }
    }


    private fun importCategory(realm: Realm, base: Base, category: Category) {
        val oldCategory = base.categories.find { it.id == category.id }
        if (oldCategory == null) {
            base.categories.add(realm.copyToRealmOrUpdate(category))
        } else {
            oldCategory.name = category.name
            oldCategory.background = category.background
            oldCategory.hidden = category.hidden
            oldCategory.layoutType = category.layoutType
            category.shortcuts.forEach { shortcut ->
                importShortcut(realm, oldCategory, shortcut)
            }
        }
    }

    private fun importShortcut(realm: Realm, category: Category, shortcut: Shortcut) {
        val oldShortcut = category.shortcuts.find { it.id == shortcut.id }
        if (oldShortcut == null) {
            category.shortcuts.add(realm.copyToRealmOrUpdate(shortcut))
        } else {
            realm.copyToRealmOrUpdate(shortcut)
        }
    }

    private fun handleError(error: Throwable): Throwable =
        getHumanReadableErrorMessage(error)
            ?.let { ImportException(it) }
            ?: error

    private fun getHumanReadableErrorMessage(e: Throwable, recursive: Boolean = true): String? = with(context) {
        when (e) {
            is JsonParseException, is JsonSyntaxException -> {
                getString(R.string.import_failure_reason_invalid_json)
            }
            is URISyntaxException,
            is IllegalArgumentException,
            is IllegalStateException,
            is IOException,
            -> {
                e.message
            }
            else -> e.cause
                ?.takeIf { recursive }
                ?.let {
                    getHumanReadableErrorMessage(it, recursive = false)
                }
        }
    }

    data class ImportStatus(val importedShortcuts: Int, val needsRussianWarning: Boolean)

}