package ch.rmy.android.framework.utils.localization

import android.content.Context

data class StaticLocalizable(val string: String) : Localizable {
    override fun localize(context: Context): CharSequence =
        string
}
