package com.afollestad.materialdialogs.prefs

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.preference.Preference
import android.util.AttributeSet

class MaterialPreference : Preference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        PrefUtil.setLayoutResource(context, this, attrs)
    }

}
