package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.R
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Variable : RealmObject(), HasId {

    @PrimaryKey
    override var id: Long = 0

    @Required
    var key: String? = null
    @Required
    var type: String? = null

    var value: String? = null
    var options: RealmList<Option>? = null

    var rememberValue: Boolean = false
    var urlEncode: Boolean = false
    var jsonEncode: Boolean = false

    var flags: Int = 0

    @Required
    var title: String? = null

    override val isNew: Boolean
        get() = id == 0L

    var isShareText: Boolean
        get() = flags and FLAG_SHARE_TEXT != 0
        set(shareText) = if (shareText) {
            flags = flags or FLAG_SHARE_TEXT
        } else {
            flags = flags and FLAG_SHARE_TEXT.inv()
        }

    fun isResetAfterUse(): Boolean {
        return !rememberValue && (TYPE_TEXT == type || TYPE_NUMBER == type || TYPE_PASSWORD == type || TYPE_COLOR == type)
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

        val TYPE_OPTIONS = arrayOf(TYPE_CONSTANT, TYPE_TEXT, TYPE_NUMBER, TYPE_PASSWORD, TYPE_COLOR, TYPE_SELECT, TYPE_TOGGLE)
        val TYPE_RESOURCES = intArrayOf(R.string.variable_type_constant, R.string.variable_type_text, R.string.variable_type_number, R.string.variable_type_password, R.string.variable_type_color, R.string.variable_type_select, R.string.variable_type_toggle)

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
