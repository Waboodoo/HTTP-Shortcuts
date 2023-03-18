package ch.rmy.android.http_shortcuts.data.migration

import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.dynamic.getValue

fun DynamicRealmObject.getString(key: String): String? =
    try {
        getValue(key)
    } catch (e: IllegalArgumentException) {
        try {
            getNullableValue(key)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
