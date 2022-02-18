package ch.rmy.android.framework.utils.localization

import android.content.Context

interface Localizable {
    fun localize(context: Context): CharSequence

    companion object {
        val EMPTY = create { "" }

        fun create(creator: (context: Context) -> CharSequence) =
            object : Localizable {
                override fun localize(context: Context): CharSequence =
                    creator(context)
            }
    }
}
