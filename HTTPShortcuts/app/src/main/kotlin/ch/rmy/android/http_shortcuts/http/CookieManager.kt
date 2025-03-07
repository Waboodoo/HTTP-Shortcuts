package ch.rmy.android.http_shortcuts.http

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.CookieJar

@Singleton
class CookieManager
@Inject
constructor(
    private val context: Context,
) {
    private val cookieSessionStore: CookieCache by lazy {
        SetCookieCache()
    }
    private val persistor by lazy {
        SharedPrefsCookiePersistor(context)
    }
    private val cookieJar by lazy {
        PersistentCookieJar(cookieSessionStore, persistor)
    }

    fun getCookieJar(): CookieJar =
        cookieJar

    fun clearCookies() {
        cookieSessionStore.clear()
        persistor.clear()
    }
}
