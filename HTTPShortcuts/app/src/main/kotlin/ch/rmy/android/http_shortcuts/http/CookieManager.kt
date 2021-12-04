package ch.rmy.android.http_shortcuts.http

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor

object CookieManager {

    fun getCookieJar(context: Context) =
        PersistentCookieJar(cookieSessionStore, SharedPrefsCookiePersistor(context))

    fun clearCookies(context: Context) {
        cookieSessionStore.clear()
        SharedPrefsCookiePersistor(context).clear()
    }

    private val cookieSessionStore: CookieCache by lazy {
        SetCookieCache()
    }
}
