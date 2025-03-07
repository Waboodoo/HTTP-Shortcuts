package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.time.Instant

data class HistoryEvent(
    val id: Int,
    val type: HistoryEventType?,
    private val data: String,
    val time: Instant,
) {
    fun <T> getEventData(dataClass: Class<T>): T =
        GsonUtil.gson.fromJson(data, dataClass)

    companion object {
        inline fun <reified T> HistoryEvent.getEventData(): T =
            getEventData(T::class.java)
    }
}
