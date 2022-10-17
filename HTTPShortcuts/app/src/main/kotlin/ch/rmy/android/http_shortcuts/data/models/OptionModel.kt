package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Option")
open class OptionModel(
    @PrimaryKey
    @Required
    var id: String = newUUID(),
    @Required
    var label: String = "",
    @Required
    var value: String = "",
) : RealmModel {

    val labelOrValue: String
        get() = label
            .ifEmpty { value }
            .ifEmpty { "-" }

    fun isSameAs(other: OptionModel) = other.label == label && other.value == value
}
