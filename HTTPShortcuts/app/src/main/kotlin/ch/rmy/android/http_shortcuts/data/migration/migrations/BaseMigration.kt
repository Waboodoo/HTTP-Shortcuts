package ch.rmy.android.http_shortcuts.data.migration.migrations

import com.google.gson.JsonObject
import io.realm.DynamicRealm

interface BaseMigration {

    val version: Int

    fun migrateRealm(realm: DynamicRealm) {

    }

    fun migrateImport(base: JsonObject) {

    }

}