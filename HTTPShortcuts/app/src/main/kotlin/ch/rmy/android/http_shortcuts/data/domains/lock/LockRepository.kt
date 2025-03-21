package ch.rmy.android.http_shortcuts.data.domains.lock

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.models.AppLock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class LockRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getLock(): AppLock? =
        get(Database::appLockDao)
            .getAppLock()

    fun observeLock(): Flow<AppLock?> =
        flow(Database::appLockDao) {
            observeAppLock()
                .distinctUntilChanged()
        }

    suspend fun setLock(passwordHash: String, useBiometrics: Boolean) {
        get(Database::appLockDao)
            .insert(
                AppLock(
                    passwordHash = passwordHash,
                    useBiometrics = useBiometrics,
                ),
            )
    }

    suspend fun removeLock() {
        get(Database::appLockDao).deleteAppLock()
    }
}
