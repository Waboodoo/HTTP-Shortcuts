package ch.rmy.android.scripting.quickjs

import ch.rmy.android.scripting.JsFunction
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.JsObjectBuilder
import ch.rmy.android.scripting.ScriptingEngine
import ch.rmy.android.scripting.ScriptingException
import com.whl.quickjs.android.QuickJSLoader
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import com.whl.quickjs.wrapper.QuickJSException

internal class QuickJsScriptingEngine : ScriptingEngine {

    private val jsContext: QuickJSContext = createInstance()
    private val functionConverter = QuickJsFunctionConverter(jsContext)

    override fun setExceptionHandler(onException: (ScriptingException) -> Unit) {
        // there is no support for an explicit exception handler
    }

    override fun evaluateScript(script: String) {
        try {
            jsContext.evaluate(script)
        } catch (e: QuickJSException) {
            throw ScriptingException(e, e.simplifiedMessage, e.lineNumber)
        }
    }

    private val QuickJSException.simplifiedMessage
        get() = message
            ?.split("at unknown.js:")
            ?.getOrNull(0)
            ?.trimEnd()

    private val QuickJSException.lineNumber: Int?
        get() = message
            ?.trimEnd('\n')
            ?.split("at unknown.js:")
            ?.getOrNull(1)
            ?.toIntOrNull()

    override fun registerFunction(name: String, function: JsFunction) {
        jsContext.setProperty(jsContext.globalObject, name, functionConverter.convert(function))
    }

    override fun registerObject(name: String, obj: JsObject?) {
        jsContext.setProperty(jsContext.globalObject, name, obj?.toJSObject())
    }

    override fun registerString(name: String, string: String?) {
        jsContext.setProperty(jsContext.globalObject, name, string)
    }

    override fun registerListOfObjects(name: String, list: List<JsObject>) {
        val array = jsContext.createNewJSArray()
        list.forEachIndexed { index, item ->
            array.set((item as QuickJsObject).toJSObject(), index)
        }
        jsContext.setProperty(jsContext.globalObject, name, array)
    }

    override fun buildJsObject(builder: JsObjectBuilder.() -> Unit): JsObject =
        QuickJsObjectBuilder(this, jsContext, functionConverter).apply(builder).build()

    private fun JsObject.toJSObject(): JSObject =
        (this as QuickJsObject).toJSObject()

    override fun destroy() {
        jsContext.destroy()
    }

    companion object {
        private var initialized = false

        fun createInstance(): QuickJSContext {
            if (!initialized) {
                QuickJSLoader.init()
                initialized = true
            }
            return QuickJSContext.create()
        }
    }
}
