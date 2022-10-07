package ch.rmy.android.http_shortcuts.data.migration

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.migration.migrations.CategoryBackgroundMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.CategoryLayoutMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ParameterTypeMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.RemoveLegacyActionsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ReplaceActionsWithScriptsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ReplaceVariableKeysWithIdsMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ResponseActionMigration
import ch.rmy.android.http_shortcuts.data.migration.migrations.ResponseHandlingMigration
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.FieldAttribute
import io.realm.RealmList
import io.realm.RealmMigration
import java.util.Date

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DatabaseMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        for (version in oldVersion + 1..newVersion) {
            migrate(realm, version)
        }
    }

    private fun migrate(realm: DynamicRealm, newVersion: Long) {
        val schema = realm.schema
        when (newVersion) {
            1L -> { // 1.10.0
                schema.get("Shortcut")!!.addField("acceptAllCertificates", Boolean::class.javaPrimitiveType)
            }
            2L -> { // 1.11.0
                val resolvedVariableSchema = schema.create("ResolvedVariable")
                    .addField("key", String::class.java)
                    .addField("value", String::class.java)
                val optionSchema = schema.create("Option")
                    .addField("label", String::class.java)
                    .addField("value", String::class.java)
                val variableSchema = schema.create("Variable")
                    .addField("id", Long::class.javaPrimitiveType, FieldAttribute.PRIMARY_KEY)
                    .addField("key", String::class.java, FieldAttribute.REQUIRED)
                    .addField("type", String::class.java, FieldAttribute.REQUIRED)
                    .addField("value", String::class.java)
                    .addField("title", String::class.java, FieldAttribute.REQUIRED)
                    .addField("urlEncode", Boolean::class.javaPrimitiveType)
                    .addField("jsonEncode", Boolean::class.javaPrimitiveType)
                    .addRealmListField("options", optionSchema)
                schema.get("Base")!!
                    .addRealmListField("variables", variableSchema)
                schema.get("PendingExecution")!!
                    .addRealmListField("resolvedVariables", resolvedVariableSchema)

                val base = realm.where("Base").findFirst()
                base?.setList("variables", RealmList<DynamicRealmObject>())
            }
            3L -> { // 1.12.0
                schema.get("Variable")!!.addField("rememberValue", Boolean::class.javaPrimitiveType)
            }
            4L -> { // 1.13.0
                schema.get("Variable")!!.addField("flags", Int::class.javaPrimitiveType)
            }
            5L -> { // 1.16.0
                schema.get("Category")!!.addField("layoutType", String::class.java)
                val categories = realm.where("Category").findAll()
                for (category in categories) {
                    category.setString("layoutType", "linear_list")
                }
            }
            6L -> { // 1.16.0
                schema.get("Shortcut")!!.addField("authentication", String::class.java)
                val shortcuts = realm.where("Shortcut").findAll()
                for (shortcut in shortcuts) {
                    if (!shortcut.getString("username").isNullOrEmpty() || !shortcut.getString("password").isNullOrEmpty()) {
                        shortcut.setString("authentication", "basic")
                    }
                }
            }
            7L -> { // 1.16.0
                schema.get("Base")!!.addField("version", Long::class.javaPrimitiveType)
            }
            8L -> { // 1.16.1
                schema.get("Shortcut")!!.addField("launcherShortcut", Boolean::class.javaPrimitiveType)
            }
            9L -> { // 1.16.2
                schema.get("Parameter")!!.addField("id", String::class.java)
                schema.get("Header")!!.addField("id", String::class.java)
                schema.get("Option")!!.addField("id", String::class.java)
                val parameters = realm.where("Parameter").findAll()
                for (parameter in parameters) {
                    parameter.setString("id", newUUID())
                }
                val headers = realm.where("Header").findAll()
                for (header in headers) {
                    header.setString("id", newUUID())
                }
                val options = realm.where("Option").findAll()
                for (option in options) {
                    option.setString("id", newUUID())
                }
                schema.get("Parameter")!!.addPrimaryKey("id")
                schema.get("Header")!!.addPrimaryKey("id")
                schema.get("Option")!!.addPrimaryKey("id")
            }
            10L -> { // 1.17.0
                val shortcuts = realm.where("Shortcut").findAll()
                for (shortcut in shortcuts) {
                    if (shortcut.getString("authentication") == null) {
                        shortcut.setString("authentication", "none")
                    }
                }
            }
            11L -> { // 1.17.0
                val pendingExecutionSchema = schema.get("PendingExecution")!!
                pendingExecutionSchema.addField("tryNumber", Int::class.javaPrimitiveType)
                pendingExecutionSchema.addField("waitUntil", Date::class.java)
            }
            12L -> { // 1.17.0
                schema.get("Variable")!!.addField("data", String::class.java)
            }
            13L -> { // 1.17.0
                schema.get("Shortcut")!!.addField("delay", Int::class.javaPrimitiveType)
            }
            14L -> { // 1.19.0
                makeNonNullable(realm, "Category", "layoutType") { "linear_list" }
                makeNonNullable(realm, "Option", "id") { newUUID() }
                makeNonNullable(realm, "Option", "label")
                makeNonNullable(realm, "Option", "value")
                makeNonNullable(realm, "ResolvedVariable", "key")
                makeNonNullable(realm, "ResolvedVariable", "value")
            }
            15L -> { // 1.19.0
                schema.get("Header")!!.setRequired("id", true)
                schema.get("PendingExecution")!!.setRequired("enqueuedAt", true)
            }
            16L -> { // 1.20.0
                schema.get("Shortcut")!!.addField("requestBodyType", String::class.java, FieldAttribute.REQUIRED)
                schema.get("Shortcut")!!.addField("contentType", String::class.java, FieldAttribute.REQUIRED)

                val shortcuts = realm.where("Shortcut").findAll()
                for (shortcut in shortcuts) {
                    shortcut.setString(
                        "requestBodyType",
                        if (shortcut.getList("parameters").isEmpty()) {
                            "custom_text"
                        } else {
                            "x_www_form_urlencode"
                        },
                    )
                    shortcut.setString("contentType", "text/plain")
                }
            }
            17L -> { // 1.21.0
                schema.get("Shortcut")!!.addField("serializedBeforeActions", String::class.java)
                schema.get("Shortcut")!!.addField("serializedSuccessActions", String::class.java)
                schema.get("Shortcut")!!.addField("serializedFailureActions", String::class.java)
                val shortcuts = realm.where("Shortcut").findAll()
                for (shortcut in shortcuts) {
                    shortcut.setString("serializedBeforeActions", "[]")
                    shortcut.setString("serializedSuccessActions", "[]")
                    shortcut.setString("serializedFailureActions", "[]")
                }
            }
            18L -> { // 1.21.0
                schema.get("Shortcut")!!.addField("executionType", String::class.java)
                val shortcuts = realm.where("Shortcut").findAll()
                for (shortcut in shortcuts) {
                    shortcut.setString("executionType", "app")
                }
            }
            19L -> { // 1.21.0
                val pendingExecutionSchema = schema.get("PendingExecution")!!
                pendingExecutionSchema.addField("waitForNetwork", Boolean::class.javaPrimitiveType)
            }
            20L -> { // 1.23.0
                schema.get("Shortcut")!!.addField("requireConfirmation", Boolean::class.javaPrimitiveType)
            }
            21L -> { // 1.23.0
                schema.create("AppLock")
                    .addField("id", Long::class.javaPrimitiveType, FieldAttribute.PRIMARY_KEY)
                    .addField("passwordHash", String::class.java, FieldAttribute.REQUIRED)
            }
            22L -> { // 1.24.0
                schema.get("Shortcut")!!
                    .removePrimaryKey()
                    .renameField("id", "oldId")
                    .addField("id", String::class.java)
                realm.where("Shortcut").findAll().forEach { shortcut ->
                    shortcut.setString("id", shortcut.getInt("oldId").toString())
                }
                schema.get("Shortcut")!!
                    .setRequired("id", true)
                    .addPrimaryKey("id")
                    .removeField("oldId")

                schema.get("Category")!!
                    .removePrimaryKey()
                    .renameField("id", "oldId")
                    .addField("id", String::class.java)
                realm.where("Category").findAll().forEach { category ->
                    category.setString("id", category.getInt("oldId").toString())
                }
                schema.get("Category")!!
                    .setRequired("id", true)
                    .addPrimaryKey("id")
                    .removeField("oldId")

                schema.get("Variable")!!
                    .removePrimaryKey()
                    .renameField("id", "oldId")
                    .addField("id", String::class.java)
                realm.where("Variable").findAll().forEach { variable ->
                    variable.setString("id", variable.getInt("oldId").toString())
                }
                schema.get("Variable")!!
                    .setRequired("id", true)
                    .addPrimaryKey("id")
                    .removeField("oldId")

                realm.where("PendingExecution").findAll().deleteAllFromRealm()
                schema.get("PendingExecution")!!
                    .removePrimaryKey()
                    .removeField("shortcutId")
                    .addField("shortcutId", String::class.java, FieldAttribute.REQUIRED)
                    .addPrimaryKey("shortcutId")
            }
            23L -> { // 1.24.0
                schema.get("Category")!!
                    .addField("background", String::class.java)
                realm.where("Category").findAll().forEach { category ->
                    category.setString("background", "white")
                }
                schema.get("Category")!!
                    .setRequired("background", true)
            }
            24L -> { // 1.24.0
                schema.get("Shortcut")!!
                    .addField("followRedirects", Boolean::class.javaPrimitiveType)
                realm.where("Shortcut").findAll().forEach { shortcut ->
                    shortcut.setBoolean("followRedirects", true)
                }
            }
            25L -> { // 1.24.0
                ReplaceVariableKeysWithIdsMigration().migrateRealm(realm)
            }
            26L -> { // 1.24.0
                schema.get("Parameter")!!.setRequired("id", true)
            }
            27L -> { // 1.24.0
                ReplaceActionsWithScriptsMigration().migrateRealm(realm)
            }
            28L -> { // 1.24.0
                schema.get("PendingExecution")!!
                    .addField("recursionDepth", Int::class.javaPrimitiveType)
            }
            29L -> { // 1.27.0
                schema.get("Category")!!
                    .addField("hidden", Boolean::class.javaPrimitiveType)
            }
            30L -> { // 1.27.0
                schema.get("Base")!!
                    .addField("title", String::class.java)
            }
            31L -> { // 1.27.0
                schema.get("Shortcut")!!.addField("quickSettingsTileShortcut", Boolean::class.javaPrimitiveType)
            }
            32L -> { // 1.27.0
                schema.get("Shortcut")!!
                    .addField("authToken", String::class.java, FieldAttribute.REQUIRED)
            }
            33L -> { // 1.28.0
                RemoveLegacyActionsMigration().migrateRealm(realm)
            }
            34L -> { // 1.29.0
                ParameterTypeMigration().migrateRealm(realm)
            }
            35L -> { // 1.29.0
                schema.get("Shortcut")!!
                    .addField("proxyHost", String::class.java)
                    .addField("proxyPort", Integer::class.java)
            }
            36L -> { // 1.30.0
                schema.createWithPrimaryKeyField("Widget", "widgetId", Int::class.javaPrimitiveType)
                    .addRealmObjectField("shortcut", schema.get("Shortcut")!!)
            }
            37L -> { // 1.30.0
                schema.get("Widget")!!
                    .addField("labelColor", String::class.java)
                    .addField("showLabel", Boolean::class.javaPrimitiveType)
                realm.where("Widget").findAll().forEach { shortcut ->
                    shortcut.setBoolean("showLabel", true)
                }
            }
            38L -> { // 1.31.0
                realm.where("PendingExecution").findAll().deleteAllFromRealm()
                schema.get("PendingExecution")!!
                    .removePrimaryKey()
                    .addField("id", String::class.java, FieldAttribute.REQUIRED, FieldAttribute.PRIMARY_KEY)
            }
            39L -> { // 1.32.1
                realm.where("Shortcut").findAll().forEach { shortcut ->
                    val executionType = shortcut.getString("executionType")
                    if (executionType != "app" && executionType != "browser" && executionType != "scripting" && executionType != "trigger") {
                        shortcut.setString("executionType", "app")
                    }
                }
            }
            40L -> { // 1.35.0
                ResponseHandlingMigration().migrateRealm(realm)
            }
            41L -> { // 1.36.0
                schema.get("Shortcut")!!
                    .addField("acceptCookies", Boolean::class.javaPrimitiveType)
            }
            42L -> { // 2.0.0
                schema.get("Base")!!
                    .addField("globalCode", String::class.java)
            }
            43L -> { // 2.0.0
                schema.get("Shortcut")!!
                    .addField("wifiSsid", String::class.java, FieldAttribute.REQUIRED)
            }
            44L -> { // 2.3.0
                schema.get("Shortcut")!!
                    .addField("clientCertAlias", String::class.java, FieldAttribute.REQUIRED)
            }
            45L -> { // 2.4.0
                schema.get("Shortcut")!!
                    .addField("clientCert", String::class.java, FieldAttribute.REQUIRED)
                realm.where("Shortcut").findAll().forEach { shortcut ->
                    val clientCertAlias = shortcut.getString("clientCertAlias")
                    if (clientCertAlias.isNotEmpty()) {
                        shortcut.setString("clientCert", "alias:$clientCertAlias")
                    }
                }
                schema.get("Shortcut")!!.removeField("clientCertAlias")
            }
            46L -> { // 2.13.0
                val idsInUse = realm.where("Shortcut")
                    .findAll()
                    .map {
                        it.getObject("responseHandling")
                            ?.getString("id")
                    }
                realm.where("ResponseHandling")
                    .findAll()
                    .forEach { responseHandling ->
                        if (responseHandling.getString("id") !in idsInUse) {
                            responseHandling.deleteFromRealm()
                        }
                    }
                schema.get("ResponseHandling")!!
                    .apply {
                        removePrimaryKey()
                        removeField("id")
                        isEmbedded = true
                    }
            }
            47L -> { // 2.13.0
                schema.get("ResolvedVariable")!!
                    .addField("id", String::class.java)
                realm.where("ResolvedVariable")
                    .findAll()
                    .forEach { resolvedVariable ->
                        resolvedVariable.setString("id", newUUID())
                    }
                schema.get("ResolvedVariable")!!
                    .setRequired("id", true)
                    .addPrimaryKey("id")
            }
            48L -> { // 2.14.0
                schema.get("Shortcut")!!
                    .addField("browserPackageName", String::class.java, FieldAttribute.REQUIRED)
            }
            49L -> { // 2.15.0
                schema.get("Category")!!
                    .addField("shortcutClickBehavior", String::class.java)
            }
            50L -> CategoryBackgroundMigration.migrateRealm(realm)
            51L -> CategoryLayoutMigration.migrateRealm(realm)
            52L -> { // 2.20.0
                schema.get("Variable")!!
                    .addField("message", String::class.java, FieldAttribute.REQUIRED)
            }
            53L -> { // 2.23.0
                ResponseActionMigration.migrateRealm(realm)
            }
            54L -> {
                schema.get("Base")!!
                    .addRealmListField("pollingShortcuts", schema.get("Shortcut"))
            }
            else -> throw IllegalArgumentException("Missing migration for version $newVersion")
        }
        updateVersionNumber(realm, newVersion)
    }

    private fun makeNonNullable(realm: DynamicRealm, tableName: String, field: String, valueGenerator: (() -> String) = { "" }) {
        val table = realm.schema.get(tableName)!!
        if (!table.isNullable(field)) {
            table.setNullable(field, true)
        }
        realm.where(tableName).isNull(field).findAll().forEach {
            it.setString(field, valueGenerator())
        }
        table.setNullable(field, false)
    }

    private fun updateVersionNumber(realm: DynamicRealm, version: Long) {
        val base = realm.where("Base").findFirst()
        if (version >= 7L) {
            base?.setLong("version", version)
        }
    }

    companion object {

        const val VERSION = 54L
    }
}
