package ch.rmy.android.http_shortcuts.import_export.migration

import com.google.gson.JsonObject

interface ImportMigration {
    fun migrateImport(base: JsonObject)
}
