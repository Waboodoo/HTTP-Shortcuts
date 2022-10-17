package ch.rmy.android.http_shortcuts.import_export

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmObject
import java.lang.reflect.Type

class ModelSerializer : JsonSerializer<RealmModel> {
    private val classMap = mutableMapOf<Class<out RealmModel>, RealmModel>()
    override fun serialize(src: RealmModel, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val instance = classMap.getOrPut(src::class.java) {
            src::class.java.getConstructor().newInstance()
        }
        val output = JsonObject()
        src::class.java.declaredFields
            .filter {
                it.declaringClass != RealmObject::class.java
            }
            .forEach { field ->
                val fieldName = field.name
                field.isAccessible = true
                val value = field.get(src)
                val defaultValue = field.get(instance)
                if (value != defaultValue) {
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
                        is RealmList<*> -> {
                            if (value.isNotEmpty()) {
                                val array = JsonArray()
                                value.forEach {
                                    array.add(context.serialize(it))
                                }
                                output.add(fieldName, array)
                            }
                        }
                        is RealmModel -> {
                            output.add(fieldName, context.serialize(value))
                        }
                    }
                }
            }
        return output
    }
}
