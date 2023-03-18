package ch.rmy.android.http_shortcuts.data.models

import io.realm.kotlin.types.EmbeddedRealmObject

class Repetition() : EmbeddedRealmObject {

    constructor(
        interval: Int = 0,
    ) : this() {
        this.interval = interval
    }

    var interval: Int = 0

    fun isSameAs(other: Repetition) =
        interval == other.interval

    fun validate() {
        require(interval >= 0) {
            "Invalid repetition interval: $interval"
        }
    }
}
