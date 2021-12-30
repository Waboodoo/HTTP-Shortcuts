package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Variable(
    @PrimaryKey
    override var id: String = "",

    @Required
    var key: String = "",
    @Required
    var type: String = TYPE_CONSTANT,

    var value: String? = "",
    var options: RealmList<Option>? = RealmList(),

    var rememberValue: Boolean = false,
    var urlEncode: Boolean = false,
    var jsonEncode: Boolean = false,

    var data: String? = null,

    var flags: Int = 0,

    @Required
    var title: String = "",
) : RealmObject(), HasId {

    var isShareText: Boolean
        get() = flags and FLAG_SHARE_TEXT != 0
        set(value) {
            flags = if (value) {
                flags or FLAG_SHARE_TEXT
            } else {
                flags and FLAG_SHARE_TEXT.inv()
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
        if (other.options!!.indices.any { !options!![it]!!.isSameAs(other.options!![it]!!) }) {
            return false
        }
        return true
    }

    var dataForType: Map<String, String?>
        get() = GsonUtil.fromJsonObject<Map<String, String?>>(data)[type]?.toMap() ?: emptyMap()
        set(value) {
            val dataMap = GsonUtil.fromJsonObject<Map<String, String?>>(data).toMutableMap()
            dataMap[type] = value
            data = GsonUtil.toJson(dataMap)
        }

    val isConstant
        get() = type == TYPE_CONSTANT

    val isRememberValueSet
        get() = rememberValue

    val isText
        get() = type == TYPE_TEXT

    val isNumber
        get() = type == TYPE_NUMBER

    val isSlider
        get() = type == TYPE_SLIDER

    override fun toString() = "Variable($type, $key, $id)"

    fun validate() {
        if (!UUIDUtils.isUUID(id) && id.toIntOrNull() == null) {
            throw IllegalArgumentException("Invalid variable ID found, must be UUID: $id")
        }

        if (type !in setOf(
                TYPE_CONSTANT,
                TYPE_TEXT,
                TYPE_NUMBER,
                TYPE_PASSWORD,
                TYPE_SELECT,
                TYPE_TOGGLE,
                TYPE_COLOR,
                TYPE_DATE,
                TYPE_TIME,
                TYPE_SLIDER,
            )
        ) {
            throw IllegalArgumentException("Invalid variable type: $type")
        }
    }

    companion object {

        const val FIELD_KEY = "key"

        const val TYPE_CONSTANT = "constant"
        const val TYPE_TEXT = "text"
        const val TYPE_NUMBER = "number"
        const val TYPE_PASSWORD = "password"
        const val TYPE_SELECT = "select"
        const val TYPE_TOGGLE = "toggle"
        const val TYPE_COLOR = "color"
        const val TYPE_DATE = "date"
        const val TYPE_TIME = "time"
        const val TYPE_SLIDER = "slider"

        private const val FLAG_SHARE_TEXT = 0x1
        private const val FLAG_MULTILINE = 0x2
    }
}
