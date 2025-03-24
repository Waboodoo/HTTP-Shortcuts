package ch.rmy.android.framework.data

@Deprecated("Use Room instead")
interface RealmFactory {
    fun getRealmContext(): RealmContext

    suspend fun updateRealm(transaction: RealmTransactionContext.() -> Unit)
}
