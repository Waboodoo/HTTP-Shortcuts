package ch.rmy.android.scripting.liquidcore

import androidx.annotation.Keep
import ch.rmy.android.scripting.JsFunction
import ch.rmy.android.scripting.JsFunctionArgsImpl
import ch.rmy.android.scripting.JsObject
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.javascript.JSUint8Array
import org.liquidplayer.javascript.JSValue

internal class LiquidCoreJsFunctionConverter(
    private val jsContext: JSContext,
) {
    fun convert(function: JsFunction): JSFunction =
        object : JSFunction(jsContext, "run") {
            @Suppress("unused")
            @Keep
            fun run(arg1: JSValue?, arg2: JSValue?, arg3: JSValue?, arg4: JSValue?): JSValue? {
                val args = JsFunctionArgsImpl(
                    listOf(
                        LiquidCoreJSValueWrapper(arg1),
                        LiquidCoreJSValueWrapper(arg2),
                        LiquidCoreJSValueWrapper(arg3),
                        LiquidCoreJSValueWrapper(arg4),
                    )
                )
                val result = function.invoke(args)
                    ?: return null
                return convertResult(result)
            }
        }

    private fun convertResult(result: Any): JSValue =
        when (result) {
            is JsObject -> (result as LiquidCoreJsObject).toJSObject()
            is ByteArray -> JSUint8Array(jsContext, result.size)
                .apply {
                    result.forEachIndexed { index, byte ->
                        set(index, byte)
                    }
                }
            else -> JSValue(jsContext, result)
        }
}
