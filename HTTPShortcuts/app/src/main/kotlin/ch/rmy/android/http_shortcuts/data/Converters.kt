package ch.rmy.android.http_shortcuts.data

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun deserializeInstant(value: Long?): Instant? =
        value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun serializeInstant(date: Instant?): Long? =
        date?.toEpochMilli()
}
