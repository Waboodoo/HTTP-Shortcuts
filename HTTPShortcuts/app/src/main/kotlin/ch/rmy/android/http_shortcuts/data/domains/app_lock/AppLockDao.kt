package ch.rmy.android.http_shortcuts.data.domains.app_lock

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.models.AppLock
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLockDao {
    @Query("SELECT * FROM app_lock")
    suspend fun getAppLock(): AppLock?

    @Query("SELECT * FROM app_lock")
    fun observeAppLock(): Flow<AppLock?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appLock: AppLock)

    @Query("DELETE FROM app_lock")
    suspend fun deleteAppLock()
}
