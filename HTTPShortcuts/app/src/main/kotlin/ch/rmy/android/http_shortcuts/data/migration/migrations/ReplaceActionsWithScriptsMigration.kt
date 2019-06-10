package ch.rmy.android.http_shortcuts.data.migration.migrations

import com.google.gson.JsonObject
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import org.json.JSONArray

class ReplaceActionsWithScriptsMigration : BaseMigration {

    override val version: Int
        get() = 27

    override fun migrateRealm(realm: DynamicRealm) {
        val schema = realm.schema

        schema.get("Shortcut")!!
            .addField("codeOnPrepare", String::class.java, FieldAttribute.REQUIRED)
            .addField("codeOnSuccess", String::class.java, FieldAttribute.REQUIRED)
            .addField("codeOnFailure", String::class.java, FieldAttribute.REQUIRED)

        realm.where("Shortcut")
            .findAll()
            .forEach { shortcut ->
                shortcut.setString("codeOnPrepare", jsonActionListToJsCode(shortcut.getString("serializedBeforeActions")))
                shortcut.setString("codeOnSuccess", jsonActionListToJsCode(shortcut.getString("serializedSuccessActions")))
                shortcut.setString("codeOnFailure", jsonActionListToJsCode(shortcut.getString("serializedFailureActions")))
            }

        schema.get("Shortcut")!!
            .removeField("serializedBeforeActions")
            .removeField("serializedSuccessActions")
            .removeField("serializedFailureActions")
    }

    private fun jsonActionListToJsCode(jsonList: String?): String {
        val codeBuilder = StringBuilder()
        val array = JSONArray(jsonList ?: "[]")
        for (i in 0..(array.length() - 1)) {
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
        for (category in base["categories"].asJsonArray) {
            for (shortcutObj in category.asJsonObject["shortcuts"].asJsonArray) {
                val shortcut = shortcutObj.asJsonObject
                shortcut.addProperty("codeOnPrepare", jsonActionListToJsCode(shortcut.get("serializedBeforeActions")?.asString))
                shortcut.addProperty("codeOnSuccess", jsonActionListToJsCode(shortcut.get("serializedSuccessActions")?.asString))
                shortcut.addProperty("codeOnFailure", jsonActionListToJsCode(shortcut.get("serializedFailureActions")?.asString))
            }
        }
    }

}