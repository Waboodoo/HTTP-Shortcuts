package ch.rmy.android.framework.data

interface RealmFactory {
    fun getRealmContext(): RealmContext

    suspend fun updateRealm(transaction: RealmTransactionContext.() -> Unit)
}
