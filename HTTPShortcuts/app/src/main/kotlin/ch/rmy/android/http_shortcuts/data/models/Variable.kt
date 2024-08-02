package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.hasDuplicatesBy
import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey

class Variable() : RealmObject {

    constructor(
        id: VariableId = "",
        key: VariableKey = "",
        value: String? = "",
        options: RealmList<Option>? = realmListOf(),
        rememberValue: Boolean = false,
        urlEncode: Boolean = false,
        jsonEncode: Boolean = false,
        title: String = "",
        message: String = "",
        variableType: VariableType = VariableType.CONSTANT,
    ) : this() {
        this.id = id
        this.key = key
        this.value = value
        this.options = options
        this.rememberValue = rememberValue
        this.urlEncode = urlEncode
        this.jsonEncode = jsonEncode
        this.title = title
        this.message = message
        type = variableType.type
    }

    @PrimaryKey
    var id: VariableId = ""
    var key: VariableKey = ""
    var value: String? = ""
    var options: RealmList<Option>? = realmListOf()
    var rememberValue: Boolean = false
    var urlEncode: Boolean = false
    var jsonEncode: Boolean = false
    var title: String = ""
    var message: String = ""

    private var flags: Int = 0

    private var type: String = VariableType.CONSTANT.type

    private var data: String? = null

    var variableType: VariableType
        get() = VariableType.parse(type)
        set(value) {
            type = value.type
        }

    var isShareText: Boolean
        get() = flags and FLAG_SHARE_TEXT != 0
        set(value) {
            flags = if (value) {
                flags or FLAG_SHARE_TEXT
            } else {
                flags and FLAG_SHARE_TEXT.inv()
            }
        }

    var isShareTitle: Boolean
        get() = flags and FLAG_SHARE_TITLE != 0
        set(value) {
            flags = if (value) {
                flags or FLAG_SHARE_TITLE
            } else {
                flags and FLAG_SHARE_TITLE.inv()
            }
        }

    var isMultiline: Boolean
        get() = flags and FLAG_MULTILINE != 0
        set(value) {
            flags = if (value) {
                flags or FLAG_MULTILINE
            } else {
                flags and FLAG_MULTILINE.inv()
            }
        }

    var isExcludeValueFromExport: Boolean
        get() = flags and FLAG_EXCLUDE_VALUE_FROM_EXPORT != 0
        set(value) {
            flags = if (value) {
                flags or FLAG_EXCLUDE_VALUE_FROM_EXPORT
            } else {
                flags and FLAG_EXCLUDE_VALUE_FROM_EXPORT.inv()
            }
        }

    fun isSameAs(other: Variable): Boolean {
        if (other.key != key ||
            other.type != type ||
            other.value != value ||
            other.title != title ||
            other.options!!.size != options!!.size ||
            other.rememberValue != rememberValue ||
            other.urlEncode != urlEncode ||
            other.jsonEncode != jsonEncode ||
            other.flags != flags ||
            other.data != data
        ) {
            return false
        }
        if (other.options!!.indices.any { !options!![it].isSameAs(other.options!![it]) }) {
            return false
        }
        return true
    }

    @Ignore
    private var dataCache: Map<String, String?>? = null

    var dataForType: Map<String, String?>
        get() = dataCache
            ?: run {
                (GsonUtil.fromJsonObject<Map<String, String?>>(data)[type]?.toMap() ?: emptyMap())
                    .also {
                        dataCache = it
                    }
            }
        set(value) {
            dataCache = value
            val dataMap = GsonUtil.fromJsonObject<Map<String, String?>>(data).toMutableMap()
            dataMap[type] = value
            data = GsonUtil.toJson(dataMap)
        }

    override fun toString() = "Variable($type, $key, $id)"

    fun validate() {
        require(id.isUUID() || id.isInt()) {
            "Invalid variable ID found, must be UUID: $id"
        }
        require(Variables.isValidVariableKey(key)) {
            "Invalid variable key: $key"
        }
        require(VariableType.entries.any { it.type == type }) {
            "Invalid variable type: $type"
        }
        options?.forEach(Option::validate)
        options?.let { options ->
            require(!options.hasDuplicatesBy { it.id }) {
                "Duplicate option IDs"
            }
        }
    }

    companion object {

        const val TEMPORARY_ID: VariableId = "0"

        const val FIELD_ID = "id"
        const val FIELD_KEY = "key"

        private const val FLAG_SHARE_TEXT = 0x1
        private const val FLAG_MULTILINE = 0x2
        private const val FLAG_SHARE_TITLE = 0x4
        private const val FLAG_EXCLUDE_VALUE_FROM_EXPORT = 0x8
    }
}
