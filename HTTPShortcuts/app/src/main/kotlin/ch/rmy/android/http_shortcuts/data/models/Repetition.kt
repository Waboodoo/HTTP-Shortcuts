package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Repetition(
    var interval: Int = 0,
) : RealmModel {
    fun isSameAs(other: Repetition) =
        interval == other.interval

    fun validate() {
        require(interval >= 0) {
            "Invalid repetition interval: $interval"
        }
    }
}
