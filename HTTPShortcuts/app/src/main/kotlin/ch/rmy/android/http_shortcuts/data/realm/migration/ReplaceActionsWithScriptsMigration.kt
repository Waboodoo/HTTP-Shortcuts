package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.migration.AutomaticSchemaMigration
import org.json.JSONArray

class ReplaceActionsWithScriptsMigration : RealmMigration {
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
}
