package ch.rmy.android.http_shortcuts.import_export

import android.net.Uri
import ch.rmy.android.http_shortcuts.data.models.ExcludedFromExport
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant

// TODO: Rethink this whole class, maybe migrate it to a TypeAdapter, or move away from GSON entirely
class ModelSerializer : JsonSerializer<Any> {
    private val classMap = mutableMapOf<Class<out Any>, Any>()
    override fun serialize(src: Any, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val instance = classMap.getOrPut(src::class.java) {
            src::class.java.getConstructor().newInstance()
        }
        val output = JsonObject()
        src::class.java.declaredFields
            .forEach { field ->
                val fieldName = field.name
                field.isAccessible = true
                val value = field.get(src)
                val defaultValue = field.get(instance)
                if (value != defaultValue && !field.isAnnotationPresent(ExcludedFromExport::class.java)) {
                    when (value) {
                        is String -> {
                            output.addProperty(fieldName, value)
                        }
                        is Boolean -> {
                            output.addProperty(fieldName, value)
                        }
                        is Number -> {
                            output.addProperty(fieldName, value)
                        }
                        is Char -> {
                            output.addProperty(fieldName, value)
                        }
                        is List<*> -> {
                            if (value.isNotEmpty()) {
                                val array = JsonArray()
                                value.forEach {
                                    array.add(context.serialize(it))
                                }
                                output.add(fieldName, array)
                            }
                        }
                        is Instant -> {
                            output.addProperty(fieldName, value.toEpochMilli())
                        }
                        is Uri -> {
                            output.addProperty(fieldName, value.toString())
                        }
                        else -> {
                            output.add(fieldName, context.serialize(value))
                        }
                    }
                }
            }
        return output
    }
}
