package ch.rmy.android.http_shortcuts.data.realm

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
class Variable() : RealmObject {
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
    var flags: Int = 0
    var type: String = VariableType.CONSTANT.type
    var data: String? = null
}
