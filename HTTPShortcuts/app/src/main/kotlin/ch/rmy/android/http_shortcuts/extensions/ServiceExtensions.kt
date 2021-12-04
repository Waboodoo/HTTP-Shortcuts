package ch.rmy.android.http_shortcuts.extensions

import android.app.Service
import android.content.Context

val Service.context: Context
    get() = this
