package ch.rmy.android.http_shortcuts.applock

import ch.rmy.android.framework.utils.ElapsedTime
import ch.rmy.android.framework.utils.ElapsedTimeProvider
import ch.rmy.android.http_shortcuts.data.domains.app_lock.AppLockRepository
import ch.rmy.android.http_shortcuts.data.models.AppLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import org.mindrot.jbcrypt.BCrypt

@Singleton
class AppLockController
@Inject
constructor(
    private val appLockRepository: AppLockRepository,
    private val elapsedTimeProvider: ElapsedTimeProvider,
) {

    private var lastUnlockCheckIn: ElapsedTime = ElapsedTime.MIN
    private var lastExplicitUnlock: ElapsedTime? = null

    private val lockFlow = appLockRepository.observeLock()
    private val temporarilyUnlockedFlow = MutableStateFlow(false)

    private val lockedFlow = combine(lockFlow, temporarilyUnlockedFlow) { lock, unlocked ->
        lock != null && !unlocked
    }

    fun observeLocked(): Flow<Boolean> = lockedFlow

    fun observeLock(): Flow<AppLock?> = lockFlow

    suspend fun getLock(): AppLock? = lockFlow.first()

    fun unlock() {
        val time = elapsedTimeProvider.get()
        lastUnlockCheckIn = time
        lastExplicitUnlock = time
        temporarilyUnlockedFlow.value = true
    }

    fun lock() {
        temporarilyUnlockedFlow.value = false
    }

    suspend fun setLock(password: String, useBiometrics: Boolean) {
        appLockRepository.setLock(BCrypt.hashpw(password, BCrypt.gensalt()), useBiometrics)
        lock()
    }

    suspend fun isPasswordCorrect(password: String): Boolean {
        val lock = appLockRepository.getLock()
            ?: return true
        return BCrypt.checkpw(password, lock.passwordHash)
    }

    suspend fun removeLock() {
        appLockRepository.removeLock()
    }

    suspend fun monitor() {
        combine(lockFlow, temporarilyUnlockedFlow) { lock, unlocked ->
            lock != null && unlocked
        }
            .distinctUntilChanged()
            .collectLatest { isTemporarilyUnlocked ->
                if (isTemporarilyUnlocked) {
                    val lockTime = lastUnlockCheckIn
                    if (lockTime + AUTO_LOCK_TIMEOUT < elapsedTimeProvider.get()) {
                        lock()
                    } else {
                        lastUnlockCheckIn = elapsedTimeProvider.get()
                        try {
                            awaitCancellation()
                        } finally {
                            lastUnlockCheckIn = elapsedTimeProvider.get()
                        }
                    }
                }
            }
    }

    fun getTimeSinceLastUnlock(): Duration? =
        lastExplicitUnlock?.let { elapsedTimeProvider.get() - it }

    companion object {
        private val AUTO_LOCK_TIMEOUT = 10.minutes
    }
}
