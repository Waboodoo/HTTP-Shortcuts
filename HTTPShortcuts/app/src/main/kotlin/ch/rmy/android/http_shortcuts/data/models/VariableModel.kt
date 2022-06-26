package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Variable")
open class VariableModel(
    @PrimaryKey
    var id: VariableId = "",

    @Required
    var key: VariableKey = "",

    var value: String? = "",
    var options: RealmList<OptionModel>? = RealmList(),

    var rememberValue: Boolean = false,
    var urlEncode: Boolean = false,
    var jsonEncode: Boolean = false,

    @Required
    var title: String = "",
    @Required
    var message: String = "",
    variableType: VariableType = VariableType.CONSTANT,
) : RealmObject() {

    private var flags: Int = 0

    @Required
    private var type: String = VariableType.CONSTANT.type

    init {
        type = variableType.type
    }

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

    fun isSameAs(other: VariableModel): Boolean {
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
        if (other.options!!.indices.any { !options!![it]!!.isSameAs(other.options!![it]!!) }) {
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
        if (!UUIDUtils.isUUID(id) && id.toIntOrNull() == null) {
            throw IllegalArgumentException("Invalid variable ID found, must be UUID: $id")
        }

        if (!Variables.isValidVariableKey(key)) {
            throw IllegalArgumentException("Invalid variable key: $key")
        }

        if (VariableType.values().none { it.type == type }) {
            throw IllegalArgumentException("Invalid variable type: $type")
        }
    }

    companion object {

        const val TEMPORARY_ID: VariableId = "0"

        const val FIELD_ID = "id"
        const val FIELD_KEY = "key"

        private const val FLAG_SHARE_TEXT = 0x1
        private const val FLAG_MULTILINE = 0x2
        private const val FLAG_SHARE_TITLE = 0x4
    }
}
