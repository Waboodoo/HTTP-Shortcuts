package ch.rmy.android.framework.utils.localization

import android.content.Context
import ch.rmy.android.framework.extensions.tryOrLog

interface Localizable {
    fun localize(context: Context): CharSequence

    companion object {
        val EMPTY = create { "" }

        fun create(creator: (context: Context) -> CharSequence) =
            object : Localizable {
                override fun localize(context: Context): CharSequence =
                    tryOrLog {
                        creator(context)
                    } ?: "???"
            }
    }
}
