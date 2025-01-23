package ch.rmy.android.scripting.quickjs

import ch.rmy.android.scripting.JsFunction
import ch.rmy.android.scripting.JsFunctionArgsImpl
import ch.rmy.android.scripting.JsObject
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.QuickJSContext

internal class QuickJsFunctionConverter(
    private val jsContext: QuickJSContext,
) {
    fun convert(function: JsFunction): JSCallFunction {
        return object : JSCallFunction {
            override fun call(vararg args: Any): Any {
                val args = JsFunctionArgsImpl(args.map { QuickJsValueWrapper(it) })
                val result = function.invoke(args)
                return convertResult(result)
            }

            private fun convertResult(result: Any?): Any =
                when (result) {
                    Unit, null -> "[[[no result]]]"
                    is JsObject -> (result as QuickJsObject).toJSObject()
                    is List<*> -> {
                        jsContext.createNewJSArray()
                            .apply {
                                result.forEachIndexed { index, item ->
                                    set(convertResult(item), index)
                                }
                            }
                    }
                    else -> result
                }
        }
    }
}
