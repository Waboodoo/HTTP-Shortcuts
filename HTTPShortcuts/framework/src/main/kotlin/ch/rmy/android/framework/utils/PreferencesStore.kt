package ch.rmy.android.framework.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

abstract class PreferencesStore(context: Context, preferencesName: String? = null) {

    private val preferences: SharedPreferences =
        if (preferencesName != null) {
            context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        } else {
            PreferenceManager.getDefaultSharedPreferences(context)
        }

    protected fun getString(key: String): String? =
        preferences.getString(key, null)

    protected fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        preferences.getBoolean(key, defaultValue)

    protected fun getInt(key: String): Int? =
        preferences.getInt(key, Int.MIN_VALUE).takeUnless { it == Int.MIN_VALUE }

    protected fun getLong(key: String): Long? =
        preferences.getLong(key, Long.MIN_VALUE).takeUnless { it == Long.MIN_VALUE }

    protected fun putString(key: String, value: String?) {
        preferences.edit { putString(key, value) }
    }

    protected fun putBoolean(key: String, value: Boolean) {
        preferences.edit { putBoolean(key, value) }
    }

    protected fun putInt(key: String, value: Int) {
        preferences.edit { putInt(key, value) }
    }

    protected fun putLong(key: String, value: Long) {
        preferences.edit { putLong(key, value) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun <T> observe(getValue: () -> T): Flow<T> =
        callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                channel.trySendBlocking(Unit)
            }
            preferences.registerOnSharedPreferenceChangeListener(listener)
            channel.send(Unit)
            awaitClose {
                preferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
            .mapLatest {
                withContext(Dispatchers.IO) {
                    getValue()
                }
            }
            .distinctUntilChanged()
}
