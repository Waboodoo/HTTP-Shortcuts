package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "global_code")
    val globalCode: String,
    @ColumnInfo(name = "user_agent")
    val userAgent: String? = null,
    @ColumnInfo(name = "default_browser")
    val defaultBrowser: TargetBrowser? = null,
)
