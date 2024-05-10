package ch.rmy.android.framework.utils.localization

import android.content.Context
import androidx.annotation.PluralsRes
import ch.rmy.android.framework.extensions.logException

class QuantityStringLocalizable(
    @PluralsRes val pluralsRes: Int,
    val count: Int,
    vararg args: Any,
) : Localizable {
    private val arguments = args.ifEmpty { arrayOf(count) }

    override fun localize(context: Context): CharSequence =
        try {
            context.resources.getQuantityString(pluralsRes, count, *localizeArguments(context))
        } catch (e: Exception) {
            logException(e)
            "-- error --"
        }

    private fun localizeArguments(context: Context) =
        arguments
            .map {
                if (it is Localizable) it.localize(context) else it
            }
            .toTypedArray()

    override fun equals(other: Any?): Boolean =
        other is QuantityStringLocalizable && pluralsRes == other.pluralsRes && count == other.count && arguments.contentEquals(other.arguments)

    override fun hashCode(): Int =
        pluralsRes + count + arguments.contentHashCode()

    override fun toString() =
        "QuantityStringLocalizable[res=$pluralsRes; count=$count${if (arguments.isNotEmpty()) "; args=${arguments.joinToString(", ")}" else ""}]"
}
