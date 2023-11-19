package ch.rmy.android.http_shortcuts.navigation

import android.util.LruCache
import ch.rmy.android.framework.utils.UUIDUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationArgStore
@Inject
constructor() {
    private val store = LruCache<ArgStoreId, Any>(5)

    fun storeArg(arg: Any): ArgStoreId {
        val id = ArgStoreId(UUIDUtils.newUUID())
        store.put(id, arg)
        return id
    }

    fun takeArg(id: ArgStoreId): Any? {
        val arg = store.get(id)
        store.remove(id)
        return arg
    }

    data class ArgStoreId(val id: String) {
        override fun toString(): String =
            id
    }
}
