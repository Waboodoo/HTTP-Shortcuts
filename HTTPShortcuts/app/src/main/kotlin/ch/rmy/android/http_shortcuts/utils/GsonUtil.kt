package ch.rmy.android.http_shortcuts.utils

import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.data.models.Base
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
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import java.io.EOFException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

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
            ?.removePrefix("Use JsonReader.setLenient(true) to accept ")
            ?.split("\nSee https")
            ?.first()
            ?.replaceFirstChar { it.uppercaseChar() }
            ?: (e.cause as? EOFException)?.message

    fun importData(data: JsonElement): Base =
        gson
            .newBuilder()
            .registerTypeAdapter(
                RealmList::class.java,
                object : JsonDeserializer<RealmList<*>> {
                    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RealmList<*> {
                        // Who needs type-safety anyway?
                        val input = json.asJsonArray
                        val output = Array<Any?>(input.size()) { null }
                        for (i in 0 until input.size()) {
                            output[i] = context.deserialize(input[i], (typeOfT as ParameterizedType).actualTypeArguments.first())
                        }
                        return realmListOf(*output)
                    }
                }
            )
            .create()
            .fromJson(data, Base::class.java)

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
            .registerTypeAdapter(RealmInstant::class.java, RealmInstantSerializer)
            .create()
    }

    object UriSerializer : JsonSerializer<Uri>, JsonDeserializer<Uri> {
        override fun serialize(src: Uri, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src.toString())

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri? =
            json?.asString?.toUri()
    }

    object RealmInstantSerializer : JsonSerializer<RealmInstant>, JsonDeserializer<RealmInstant> {
        override fun serialize(src: RealmInstant, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src.epochSeconds)

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RealmInstant? =
            json?.asLong?.let { RealmInstant.from(it, 0) }
    }
}
