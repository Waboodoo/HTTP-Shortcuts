package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.realm.RealmModel
import io.realm.annotations.Ignore
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import java.util.Date

@RealmClass(name = "HistoryEvent")
open class HistoryEventModel(
    id: String = "",
    time: Date = Date(),
    eventType: HistoryEventType? = null,
    eventData: Any? = null,
) : RealmModel {

    @PrimaryKey
    @Required
    var id: String
        private set

    @Index
    @Required
    var time: Date
        private set

    @Required
    private var type: String = ""

    @Required
    private var data: String = ""

    @delegate:Ignore
    val eventType: HistoryEventType? by lazy {
        HistoryEventType.parse(type)
    }

    fun <T> getEventData(dataClass: Class<T>): T =
        GsonUtil.gson.fromJson(data, dataClass)

    init {
        this.id = id
        this.time = time
        this.type = eventType?.type ?: ""
        this.data = GsonUtil.toJson(eventData)
    }

    companion object {
        const val FIELD_TIME = "time"

        inline fun <reified T> HistoryEventModel.getEventData(): T =
            getEventData(T::class.java)
    }
}
