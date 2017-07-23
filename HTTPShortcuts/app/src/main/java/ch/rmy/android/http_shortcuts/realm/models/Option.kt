package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Option : RealmObject() {

    @PrimaryKey
    var id: String? = null

    var label: String? = null
    var value: String? = null

    companion object {

        fun createNew(label: String, value: String): Option {
            val option = Option()
            option.id = UUIDUtils.create()
            option.label = label
            option.value = value
            return option
        }
    }

}
