package ch.rmy.android.http_shortcuts.data.realm.migration

import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.dynamic.getValue

fun DynamicRealmObject.getString(key: String): String? =
    try {
        getValue(key)
    } catch (_: IllegalArgumentException) {
        try {
            getNullableValue(key)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
