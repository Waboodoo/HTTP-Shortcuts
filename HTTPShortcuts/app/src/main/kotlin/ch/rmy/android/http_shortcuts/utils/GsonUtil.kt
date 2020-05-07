package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.http_shortcuts.data.models.Base
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.realm.RealmObject

object GsonUtil {

    fun prettyPrint(jsonString: String): String =
        try {
            val json = JsonParser.parseString(jsonString)
            val gson = GsonBuilder().setPrettyPrinting().create()
            gson.toJson(json)
        } catch (e: JsonParseException) {
            jsonString
        }

    fun importData(data: JsonElement): Base = gson.fromJson(data, Base::class.java)

    fun <T> fromJsonObject(jsonObject: String?): Map<String, T> {
        if (jsonObject == null) {
            return emptyMap()
        }
        val type = object : TypeToken<Map<String, T>>() {
        }.type
        return gson.fromJson(jsonObject, type)
    }

    fun toJson(item: Map<String, Any>): String = gson.toJson(item)

    val gson: Gson by lazy {
        GsonBuilder()
            .addSerializationExclusionStrategy(RealmExclusionStrategy())
            .create()
    }

    private class RealmExclusionStrategy : ExclusionStrategy {

        override fun shouldSkipField(f: FieldAttributes) = f.declaringClass == RealmObject::class.java

        override fun shouldSkipClass(clazz: Class<*>) = false

    }

}
