package ch.rmy.android.http_shortcuts.data

import androidx.room.TypeConverter
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import java.time.Instant

class Converters {
    @TypeConverter
    fun deserializeInstant(value: Long?): Instant? =
        value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun serializeInstant(date: Instant?): Long? =
        date?.toEpochMilli()

    @TypeConverter
    fun deserializeHistoryEventType(value: String?): HistoryEventType? =
        value?.let { HistoryEventType.parse(it) }

    @TypeConverter
    fun serializeHistoryEventType(historyEventType: HistoryEventType?): String? =
        historyEventType?.type
}
