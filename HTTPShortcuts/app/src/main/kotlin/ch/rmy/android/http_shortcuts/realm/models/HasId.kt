package ch.rmy.android.http_shortcuts.realm.models

interface HasId {

    var id: String

    val isNew: Boolean
        get() = id.isEmpty()

    companion object {

        const val FIELD_ID = "id"

    }

}
