package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required


open class Variable : RealmObject(), HasId {

    @PrimaryKey
    override var id: Long = 0

    @Required
    var key: String = ""
    @Required
    var type: String = ""

    var value: String? = null
    var options: RealmList<Option>? = null

    var rememberValue: Boolean = false
    var urlEncode: Boolean = false
    var jsonEncode: Boolean = false

    var data: String? = null

    var flags: Int = 0

    @Required
    var title: String = ""

    override val isNew: Boolean
        get() = id == 0L

    var isShareText: Boolean
        get() = flags and FLAG_SHARE_TEXT != 0
        set(shareText) = if (shareText) {
            flags = flags or FLAG_SHARE_TEXT
        } else {
            flags = flags and FLAG_SHARE_TEXT.inv()
        }

    fun isResetAfterUse(): Boolean =
            !rememberValue && type in listOf(TYPE_TEXT, TYPE_NUMBER, TYPE_PASSWORD, TYPE_COLOR, TYPE_SLIDER)

    fun isSameAs(other: Variable): Boolean {
        if (other.key != key ||
                other.type != type ||
                other.value != value ||
                other.title != title ||
                other.options!!.size != options!!.size
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

        val TYPE_OPTIONS = arrayOf(TYPE_CONSTANT, TYPE_TEXT, TYPE_NUMBER, TYPE_PASSWORD, TYPE_DATE, TYPE_TIME, TYPE_COLOR, TYPE_SELECT, TYPE_TOGGLE, TYPE_SLIDER)
        val TYPE_RESOURCES = intArrayOf(R.string.variable_type_constant, R.string.variable_type_text, R.string.variable_type_number, R.string.variable_type_password, R.string.variable_type_date, R.string.variable_type_time, R.string.variable_type_color, R.string.variable_type_select, R.string.variable_type_toggle, R.string.variable_type_slider)

        private const val FLAG_SHARE_TEXT = 0x1

        fun createNew(): Variable {
            val variable = Variable()
            variable.key = ""
            variable.type = TYPE_CONSTANT
            variable.value = ""
            variable.title = ""
            variable.options = RealmList<Option>()
            return variable
        }

    }

}
