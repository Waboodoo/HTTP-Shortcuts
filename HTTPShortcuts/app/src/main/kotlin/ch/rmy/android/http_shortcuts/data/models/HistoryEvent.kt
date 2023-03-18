package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.toInstant
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.Instant

class HistoryEvent() : RealmObject {

    constructor(
        id: String = "",
        eventType: HistoryEventType? = null,
        eventData: Any? = null,
    ) : this() {
        this.id = id
        this.type = eventType?.type ?: ""
        this.data = GsonUtil.toJson(eventData)
    }

    @PrimaryKey
    var id: String = ""
        private set

    @Index
    private var time: RealmInstant = RealmInstant.now()

    val eventTime: Instant
        get() = time.toInstant()

    private var type: String = ""

    private var data: String = ""

    @delegate:Ignore
    val eventType: HistoryEventType? by lazy {
        HistoryEventType.parse(type)
    }

    fun <T> getEventData(dataClass: Class<T>): T =
        GsonUtil.gson.fromJson(data, dataClass)

    companion object {
        const val FIELD_TIME = "time"

        inline fun <reified T> HistoryEvent.getEventData(): T =
            getEventData(T::class.java)
    }
}
