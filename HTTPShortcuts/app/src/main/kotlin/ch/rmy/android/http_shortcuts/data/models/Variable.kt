package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import org.json.JSONException
import org.json.JSONObject

@Entity(tableName = "variable")
data class Variable(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: VariableId,
    @ColumnInfo(name = "key")
    val key: VariableKey,
    @ColumnInfo(name = "variable_type")
    val type: VariableType,
    @ColumnInfo(name = "value")
    val value: String?,
    @ColumnInfo(name = "data")
    val data: String?,
    @ColumnInfo(name = "remember_value")
    val rememberValue: Boolean,
    @ColumnInfo(name = "url_encode")
    val urlEncode: Boolean,
    @ColumnInfo(name = "json_encode")
    val jsonEncode: Boolean,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "share_text")
    val isShareText: Boolean,
    @ColumnInfo(name = "share_title")
    val isShareTitle: Boolean,
    @ColumnInfo(name = "multiline")
    val isMultiline: Boolean,
    @ColumnInfo(name = "exclude_from_export")
    val isExcludeValueFromExport: Boolean,
    @ColumnInfo(name = "sorting_order", index = true)
    val sortingOrder: Int = 0,
) {
    // TODO(room): Find a better way to store values changed during execution
    @Ignore
    var valueOverride: String? = null

    val realValue: String?
        get() = valueOverride ?: value

    private val dataCache: JSONObject by lazy(LazyThreadSafetyMode.NONE) {
        try {
            JSONObject(data ?: "{}")
        } catch (_: JSONException) {
            JSONObject()
        }
    }

    fun getStringData(key: String): String? =
        if (dataCache.has(key)) {
            dataCache.optString(key)
        } else {
            null
        }

    fun getBooleanData(key: String): Boolean? =
        if (dataCache.has(key)) {
            dataCache.optBoolean(key)
        } else {
            null
        }

    fun getStringListData(key: String): List<String>? =
        if (dataCache.has(key)) {
            dataCache.optJSONArray(key)?.let { array ->
                List(array.length()) { array.optString(it) }
            }
        } else {
            null
        }

    companion object {
        const val TEMPORARY_ID: VariableId = "0"
    }
}
