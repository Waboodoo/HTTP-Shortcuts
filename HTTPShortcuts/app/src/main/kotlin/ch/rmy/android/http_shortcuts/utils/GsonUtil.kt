package ch.rmy.android.http_shortcuts.utils

import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.data.models.BaseModel
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import io.realm.RealmObject
import java.lang.reflect.Type

object GsonUtil {

    fun prettyPrint(jsonString: String): String =
        try {
            val json = JsonParser.parseString(jsonString)
            val gson = GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
            gson.toJson(json)
        } catch (e: JsonParseException) {
            jsonString
        }

    fun importData(data: JsonElement): BaseModel = gson.fromJson(data, BaseModel::class.java)

    fun <T> fromJsonObject(jsonObject: String?): Map<String, T> {
        if (jsonObject == null) {
            return emptyMap()
        }
        val type = object : TypeToken<Map<String, T>>() {
        }.type
        return gson.fromJson(jsonObject, type)
    }

    fun toJson(item: Any?): String = gson.toJson(item)

    val gson: Gson by lazy {
        GsonBuilder()
            .addSerializationExclusionStrategy(RealmExclusionStrategy())
            .registerTypeAdapter(Uri::class.java, UriSerializer)
            .create()
    }

    object UriSerializer : JsonSerializer<Uri>, JsonDeserializer<Uri> {
        override fun serialize(src: Uri, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src.toString())

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri? =
            json?.asString?.toUri()
    }

    private class RealmExclusionStrategy : ExclusionStrategy {

        override fun shouldSkipField(f: FieldAttributes) = f.declaringClass == RealmObject::class.java

        override fun shouldSkipClass(clazz: Class<*>) = false
    }
}
