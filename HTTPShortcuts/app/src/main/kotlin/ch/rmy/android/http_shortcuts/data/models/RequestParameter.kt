package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType

@Entity(tableName = "request_parameter")
data class RequestParameter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: RequestParameterId = 0,
    @ColumnInfo(name = "shortcut_id", index = true)
    val shortcutId: ShortcutId,
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "value")
    val value: String,
    @ColumnInfo(name = "type")
    val parameterType: ParameterType,
    @ColumnInfo(name = "file_upload_type")
    val fileUploadType: FileUploadType?,
    @ColumnInfo(name = "file_upload_file_name")
    val fileUploadFileName: String?,
    @ColumnInfo(name = "file_upload_source_file")
    val fileUploadSourceFile: String?,
    @ColumnInfo(name = "file_upload_use_image_editor")
    val fileUploadUseImageEditor: Boolean,
    @ColumnInfo(name = "sorting_order", index = true)
    val sortingOrder: Int = 0,
)
