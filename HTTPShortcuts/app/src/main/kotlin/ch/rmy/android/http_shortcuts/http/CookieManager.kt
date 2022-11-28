package ch.rmy.android.http_shortcuts.http

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.CookieJar
import javax.inject.Inject

class CookieManager
@Inject
constructor(
    private val context: Context,
) {

    fun getCookieJar(): CookieJar =
        PersistentCookieJar(cookieSessionStore, SharedPrefsCookiePersistor(context))

    fun clearCookies() {
        cookieSessionStore.clear()
        SharedPrefsCookiePersistor(context).clear()
    }

    companion object {
        internal val cookieSessionStore: CookieCache by lazy {
            SetCookieCache()
        }
    }
}
