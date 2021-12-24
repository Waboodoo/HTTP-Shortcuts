package ch.rmy.android.framework.utils.localization

import android.content.Context
import androidx.annotation.StringRes

class StringResLocalizable(
    @StringRes
    private val stringRes: Int,
    vararg args: Any,
) : Localizable {
    private val arguments = args

    override fun localize(context: Context) =
        context.getString(stringRes, *localizeArguments(context))

    private fun localizeArguments(context: Context) =
        arguments
            .map {
                if (it is Localizable) it.localize(context) else it
            }
            .toTypedArray()

    override fun equals(other: Any?): Boolean =
        other is StringResLocalizable && stringRes == other.stringRes && arguments.contentEquals(other.arguments)

    override fun hashCode(): Int =
        stringRes + arguments.contentHashCode()

    override fun toString() =
        "StringResLocalizable[res=$stringRes${if (arguments.isNotEmpty()) "; args=${arguments.joinToString(", ")}" else ""}]"
}
