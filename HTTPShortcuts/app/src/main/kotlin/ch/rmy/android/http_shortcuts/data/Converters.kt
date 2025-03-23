package ch.rmy.android.http_shortcuts.data

import android.net.Uri
import androidx.room.TypeConverter
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.data.enums.VariableType
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

    @TypeConverter
    fun deserializeUri(value: String?): Uri? =
        value?.let(Uri::parse)

    @TypeConverter
    fun serializeUri(uri: Uri?): String? =
        uri?.toString()

    @TypeConverter
    fun deserializeVariableType(value: String?): VariableType? =
        value?.let { VariableType.parse(it) }

    @TypeConverter
    fun serializeVariableType(variableType: VariableType): String =
        variableType.type
}
