package ch.rmy.android.http_shortcuts.data.domains.app_lock

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.AppLock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AppLockRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getLock(): AppLock? = query {
        appLockDao()
            .getAppLock()
    }

    fun observeLock(): Flow<AppLock?> = queryFlow {
        appLockDao()
            .observeAppLock()
    }

    suspend fun setLock(passwordHash: String, useBiometrics: Boolean) = query {
        appLockDao()
            .insert(
                AppLock(
                    passwordHash = passwordHash,
                    useBiometrics = useBiometrics,
                ),
            )
    }

    suspend fun removeLock() = query {
        appLockDao().deleteAppLock()
    }
}
