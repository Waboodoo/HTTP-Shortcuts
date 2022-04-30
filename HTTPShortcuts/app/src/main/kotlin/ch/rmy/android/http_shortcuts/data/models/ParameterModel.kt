package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Parameter")
open class ParameterModel(
    @PrimaryKey
    @Required
    var id: String = newUUID(),
    @Required
    var key: String = "",
    @Required
    var value: String = "",
    parameterType: ParameterType = ParameterType.STRING,
    var fileName: String = "",
) : RealmObject() {

    @Required
    private var type: String = ParameterType.STRING.type

    var parameterType: ParameterType
        get() = ParameterType.parse(type)
        set(value) {
            type = value.type
        }

    init {
        type = parameterType.type
    }

    fun isSameAs(other: ParameterModel) =
        other.key == key &&
            other.value == value &&
            other.type == type &&
            other.fileName == fileName

    val isStringParameter: Boolean
        get() = type == ParameterType.STRING.type
}
