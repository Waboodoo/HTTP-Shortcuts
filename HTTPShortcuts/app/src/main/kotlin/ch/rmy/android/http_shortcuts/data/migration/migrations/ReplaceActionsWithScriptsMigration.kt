package ch.rmy.android.http_shortcuts.data.migration.migrations

import ch.rmy.android.http_shortcuts.data.migration.getObjectArray
import ch.rmy.android.http_shortcuts.data.migration.getString
import com.google.gson.JsonObject
import io.realm.kotlin.migration.AutomaticSchemaMigration
import org.json.JSONArray

class ReplaceActionsWithScriptsMigration : BaseMigration {

    override fun migrateRealm(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate("Shortcut") { oldShortcut, newShortcut ->
            newShortcut?.set("codeOnPrepare", jsonActionListToJsCode(oldShortcut.getString("serializedBeforeActions") ?: ""))
            newShortcut?.set("codeOnSuccess", jsonActionListToJsCode(oldShortcut.getString("serializedSuccessActions") ?: ""))
            newShortcut?.set("codeOnFailure", jsonActionListToJsCode(oldShortcut.getString("serializedFailureActions") ?: ""))
        }
    }

    private fun jsonActionListToJsCode(jsonList: String?): String {
        val codeBuilder = StringBuilder()
        val array = JSONArray(jsonList ?: "[]")
        for (i in 0 until array.length()) {
            val action = array.getJSONObject(i)
            codeBuilder.append("_runAction(\"")
            codeBuilder.append(action.getString("type"))
            codeBuilder.append("\", ")
            codeBuilder.append(action.getJSONObject("data").toString())
            codeBuilder.append("); /* built-in */\n")
        }
        return codeBuilder.toString()
    }

    override fun migrateImport(base: JsonObject) {
        for (category in base.getObjectArray("categories")) {
            for (shortcut in category.getObjectArray("shortcuts")) {
                shortcut.addProperty("codeOnPrepare", jsonActionListToJsCode(shortcut.getString("serializedBeforeActions")))
                shortcut.addProperty("codeOnSuccess", jsonActionListToJsCode(shortcut.getString("serializedSuccessActions")))
                shortcut.addProperty("codeOnFailure", jsonActionListToJsCode(shortcut.getString("serializedFailureActions")))
            }
        }
    }
}
