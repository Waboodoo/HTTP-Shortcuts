package ch.rmy.android.http_shortcuts.utils

import android.view.View

var View.visible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        this.visibility = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }