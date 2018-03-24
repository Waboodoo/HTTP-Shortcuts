package ch.rmy.android.http_shortcuts.realm.models

interface HasId {

    var id: Long

    val isNew: Boolean

    companion object {

        const val FIELD_ID = "id"

    }

}
