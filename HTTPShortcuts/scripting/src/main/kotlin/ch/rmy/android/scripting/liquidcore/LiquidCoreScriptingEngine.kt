package ch.rmy.android.scripting.liquidcore

import ch.rmy.android.scripting.JsFunction
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.JsObjectBuilder
import ch.rmy.android.scripting.ScriptingEngine
import ch.rmy.android.scripting.ScriptingException
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSException
import org.liquidplayer.javascript.JSObject

internal class LiquidCoreScriptingEngine : ScriptingEngine {
    private val jsContext = JSContext()
    private val functionConverter = LiquidCoreJsFunctionConverter(jsContext)

    override fun evaluateScript(script: String) {
        try {
            jsContext.evaluateScript(script)
        } catch (e: JSException) {
            throw ScriptingException(
                e,
                if (e.error.message() == "java.lang.reflect.InvocationTargetException") {
                    "Invalid function arguments"
                } else {
                    e.message
                },
                e.lineNumber,
            )
        }
    }

    private fun wrapException(e: JSException): ScriptingException =
        ScriptingException(
            e,
            if (e.error.message() == "java.lang.reflect.InvocationTargetException") {
                "Invalid function arguments"
            } else {
                e.message
            },
            e.lineNumber,
        )

    override fun setExceptionHandler(onException: (ScriptingException) -> Unit) {
        jsContext.setExceptionHandler { e ->
            onException(wrapException(e))
        }
    }

    private val JSException.lineNumber: Int?
        get() = error
            ?.stack()
            ?.split("\n")
            ?.getOrNull(1)
            ?.trim(')')
            ?.split(':')
            ?.dropLast(1)
            ?.lastOrNull()
            ?.toInt()

    override fun registerFunction(name: String, function: JsFunction) {
        jsContext.property(
            name,
            functionConverter.convert(function),
        )
    }

    override fun registerObject(name: String, obj: JsObject?) {
        jsContext.property(name, obj?.toJSObject(), READ_ONLY)
    }

    override fun registerString(name: String, string: String?) {
        jsContext.property(name, string, READ_ONLY)
    }

    override fun registerListOfObjects(name: String, list: List<JsObject>) {
        jsContext.property(name, list.map { it.toJSObject() }, READ_ONLY)
    }

    override fun buildJsObject(builder: JsObjectBuilder.() -> Unit): JsObject =
        LiquidCoreJsObjectBuilder(jsContext, functionConverter).apply(builder).build()

    internal fun JsObject.toJSObject(): JSObject =
        (this as LiquidCoreJsObject).toJSObject()

    companion object {
        private const val READ_ONLY =
            JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete
    }
}
