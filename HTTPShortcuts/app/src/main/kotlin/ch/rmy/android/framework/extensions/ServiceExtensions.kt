package ch.rmy.android.framework.extensions

import android.app.Service
import android.content.Context

val Service.context: Context
    get() = this
