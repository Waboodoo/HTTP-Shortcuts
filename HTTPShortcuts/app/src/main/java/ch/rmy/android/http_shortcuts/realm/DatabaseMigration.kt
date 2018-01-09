package ch.rmy.android.http_shortcuts.realm

import android.text.TextUtils
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.RealmList
import io.realm.RealmMigration
import java.util.*

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
                schema.get("Shortcut").addField("acceptAllCertificates", Boolean::class.javaPrimitiveType)
            }
            2L -> { // 1.11.0
                val resolvedVariableSchema = schema.create("ResolvedVariable")
                        .addField("key", String::class.java)
                        .addField("value", String::class.java)
                val optionSchema = schema.create("Option")
                        .addField("label", String::class.java)
                        .addField("value", String::class.java)
                val variableSchema = schema.create("Variable")
                        .addField("id", Long::class.javaPrimitiveType).addPrimaryKey("id")
                        .addField("key", String::class.java).setRequired("key", true)
                        .addField("type", String::class.java).setRequired("type", true)
                        .addField("value", String::class.java)
                        .addField("title", String::class.java).setRequired("title", true)
                        .addField("urlEncode", Boolean::class.javaPrimitiveType)
                        .addField("jsonEncode", Boolean::class.javaPrimitiveType)
                        .addRealmListField("options", optionSchema)
                schema.get("Base")
                        .addRealmListField("variables", variableSchema)
                schema.get("PendingExecution")
                        .addRealmListField("resolvedVariables", resolvedVariableSchema)

                val base = realm.where("Base").findFirst()
                base?.setList("variables", RealmList<DynamicRealmObject>())
            }
            3L -> { // 1.12.0
                schema.get("Variable").addField("rememberValue", Boolean::class.javaPrimitiveType)
            }
            4L -> { // 1.13.0
                schema.get("Variable").addField("flags", Int::class.javaPrimitiveType)
            }
            5L -> { // 1.16.0
                schema.get("Category").addField("layoutType", String::class.java)
                val categories = realm.where("Category").findAll()
                for (category in categories) {
                    category.setString("layoutType", "linear_list")
                }
            }
            6L -> { // 1.16.0
                schema.get("Shortcut").addField("authentication", String::class.java)
                val shortcuts = realm.where("Shortcut").findAll()
                for (shortcut in shortcuts) {
                    if (!TextUtils.isEmpty(shortcut.getString("username")) || !TextUtils.isEmpty(shortcut.getString("password"))) {
                        shortcut.setString("authentication", "basic")
                    }
                }
            }
            7L -> { // 1.16.0
                schema.get("Base").addField("version", Long::class.javaPrimitiveType)
            }
            8L -> { // 1.16.1
                schema.get("Shortcut").addField("launcherShortcut", Boolean::class.javaPrimitiveType)
            }
            9L -> { // 1.16.2
                schema.get("Parameter").addField("id", String::class.java)
                schema.get("Header").addField("id", String::class.java)
                schema.get("Option").addField("id", String::class.java)
                val parameters = realm.where("Parameter").findAll()
                for (parameter in parameters) {
                    parameter.setString("id", UUIDUtils.create())
                }
                val headers = realm.where("Header").findAll()
                for (header in headers) {
                    header.setString("id", UUIDUtils.create())
                }
                val options = realm.where("Option").findAll()
                for (option in options) {
                    option.setString("id", UUIDUtils.create())
                }
                schema.get("Parameter").addPrimaryKey("id")
                schema.get("Header").addPrimaryKey("id")
                schema.get("Option").addPrimaryKey("id")
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
                val pendingExecutionSchema = schema.get("PendingExecution")
                pendingExecutionSchema.addField("tryNumber", Int::class.javaPrimitiveType)
                pendingExecutionSchema.addField("waitUntil", Date::class.java)
            }
            12L -> { // 1.17.0
                schema.get("Variable").addField("data", String::class.java)
            }
            13L -> { // 1.17.0
                schema.get("Shortcut").addField("delay", Int::class.javaPrimitiveType)
            }

            else -> throw IllegalArgumentException("Missing migration for version " + newVersion)
        }

        updateVersionNumber(realm, newVersion)
    }

    private fun updateVersionNumber(realm: DynamicRealm, version: Long) {
        val base = realm.where("Base").findFirst()
        if (version >= 7L) {
            base?.setLong("version", version)
        }
    }

    companion object {

        const val VERSION = 13L

    }

}
