package ch.rmy.android.http_shortcuts.utils

import android.net.Uri
import androidx.core.net.toUri
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
import com.google.gson.stream.MalformedJsonException
import java.io.EOFException
import java.lang.reflect.Type
import java.time.Instant

object GsonUtil {

    fun prettyPrintOrThrow(json: JsonElement): String {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
        return gson.toJson(json)
    }

    fun prettyPrintOrThrow(jsonString: String): String =
        prettyPrintOrThrow(JsonParser.parseString(jsonString))

    fun extractErrorMessage(e: JsonParseException): String? =
        (e.cause as? MalformedJsonException)?.message
            ?.removePrefix("Use JsonReader.setStrictness(Strictness.LENIENT) to accept ")
            ?.split("\nSee https")
            ?.first()
            ?.replaceFirstChar { it.uppercaseChar() }
            ?: (e.cause as? EOFException)?.message

    inline fun <reified T> fromJsonObject(jsonObject: String?): Map<String, T> {
        if (jsonObject == null) {
            return emptyMap()
        }
        val type = object : TypeToken<Map<String, T>>() {
        }.type
        return gson.fromJson(jsonObject, type)
    }

    fun toJson(item: Any?): String =
        gson.toJson(item)

    val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriSerializer)
            .registerTypeAdapter(Instant::class.java, InstantSerializer)
            .create()
    }

    object UriSerializer : JsonSerializer<Uri>, JsonDeserializer<Uri> {
        override fun serialize(src: Uri, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src.toString())

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri? =
            json?.asString?.toUri()
    }

    object InstantSerializer : JsonSerializer<Instant>, JsonDeserializer<Instant> {
        override fun serialize(src: Instant, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src.toEpochMilli())

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Instant? =
            json?.asLong?.let { Instant.ofEpochMilli(it) }
    }
}
