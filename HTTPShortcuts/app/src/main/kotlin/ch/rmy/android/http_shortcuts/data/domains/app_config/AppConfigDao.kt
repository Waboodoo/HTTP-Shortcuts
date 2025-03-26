package ch.rmy.android.http_shortcuts.data.domains.app_config

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config")
    suspend fun getAppConfig(): AppConfig?

    @Query("SELECT * FROM app_config")
    fun observeAppConfig(): Flow<AppConfig?>

    @Transaction
    suspend fun update(transform: (AppConfig) -> AppConfig) {
        (
            getAppConfig()
                ?: AppConfig(
                    title = "",
                    globalCode = "",
                )
            )
            .let(transform)
            .let {
                insert(it)
            }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appConfig: AppConfig)
}
