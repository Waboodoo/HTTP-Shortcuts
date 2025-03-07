package ch.rmy.android.http_shortcuts.activities.response.usecases

import ch.rmy.android.http_shortcuts.activities.response.models.TableData
import com.google.gson.JsonElement
import javax.inject.Inject

class GetTableDataUseCase
@Inject
constructor() {
    operator fun invoke(element: JsonElement): TableData? {
        val array = when {
            element.isJsonArray -> {
                element.asJsonArray
            }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                obj.keySet()
                    .singleOrNull()
                    ?.let { key ->
                        obj.get(key)
                    }
                    ?.takeIf { it.isJsonArray }
                    ?.asJsonArray
            }
            else -> null
        }
            ?: return null
        val rows = mutableListOf<Map<String, String>>()
        val columns = mutableListOf<String>()
        array.forEach {
            if (!it.isJsonObject) {
                return null
            }
            val row = it.asJsonObject
            row.keySet().forEach { key ->
                if (key !in columns) {
                    columns.add(key)
                }
            }
            rows.add(
                row.entrySet().associate { (key, element) ->
                    key to element.toSimpleString()
                },
            )
        }
        return TableData(columns, rows)
    }

    private fun JsonElement.toSimpleString(): String =
        if (isJsonPrimitive) {
            asString
        } else {
            toString()
        }
}
