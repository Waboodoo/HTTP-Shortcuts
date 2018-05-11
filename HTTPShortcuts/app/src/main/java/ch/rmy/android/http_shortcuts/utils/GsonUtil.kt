package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.realm.models.Base
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.realm.RealmObject
import java.io.Reader

object GsonUtil {

    fun prettyPrint(jsonString: String): String {
        return try {
            val parser = JsonParser()
            val json = parser.parse(jsonString)
            val gson = GsonBuilder().setPrettyPrinting().create()
            gson.toJson(json)
        } catch (e: JsonParseException) {
            jsonString
        }
    }

    fun toJson(item: RealmObject): String = gson.toJson(item)

    fun <T : RealmObject> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)

    fun exportData(base: Base, writer: Appendable) {
        getPrettyGson().toJson(base, writer)
    }

    fun exportData(base: Base): String = getPrettyGson().toJson(base)

    fun importData(reader: Reader): Base = gson.fromJson(reader, Base::class.java)

    fun <T> fromJsonObject(jsonObject: String?): Map<String, T> {
        if (jsonObject == null) {
            return emptyMap()
        }
        val type = object : TypeToken<Map<String, T>>() {
        }.type
        return gson.fromJson(jsonObject, type)
    }

    fun parseActionList(jsonList: String?): List<ActionDTO> {
        if (jsonList == null) {
            return emptyList()
        }
        val type = object : TypeToken<List<ActionDTO>>() {
        }.type
        return gson.fromJson<List<ActionDTO>>(jsonList, type)
    }

    fun toJson(item: Map<String, Any>): String = gson.toJson(item)

    fun toJson(list: List<Any>): String = gson.toJson(list)

    fun parseString(jsonString: String): JsonElement = JsonParser().parse(jsonString)

    private val gson: Gson by lazy {
        GsonBuilder()
                .addSerializationExclusionStrategy(RealmExclusionStrategy())
                .create()
    }

    private fun getPrettyGson() =
            GsonBuilder()
                    .addSerializationExclusionStrategy(RealmExclusionStrategy())
                    .setPrettyPrinting()
                    .create()

    private class RealmExclusionStrategy : ExclusionStrategy {

        override fun shouldSkipField(f: FieldAttributes) = f.declaringClass == RealmObject::class.java

        override fun shouldSkipClass(clazz: Class<*>) = false

    }

}
