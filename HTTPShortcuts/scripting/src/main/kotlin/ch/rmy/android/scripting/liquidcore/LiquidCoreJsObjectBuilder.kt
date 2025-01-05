package ch.rmy.android.scripting.liquidcore

import ch.rmy.android.scripting.JsFunction
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.JsObjectBuilder
import ch.rmy.android.scripting.ScriptingEngine
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSObject

internal class LiquidCoreJsObjectBuilder(
    override val scriptingEngine: ScriptingEngine,
    jsContext: JSContext,
    private val functionConverter: LiquidCoreJsFunctionConverter,
) : JsObjectBuilder {
    private val jsObject = JSObject(jsContext)

    override fun property(name: String, value: String?) {
        jsObject.property(name, value)
    }

    override fun property(name: String, value: Int?) {
        jsObject.property(name, value)
    }

    override fun property(name: String, value: Long?) {
        jsObject.property(name, value)
    }

    override fun property(name: String, value: Float?) {
        jsObject.property(name, value)
    }

    override fun property(name: String, value: Double?) {
        jsObject.property(name, value)
    }

    override fun property(name: String, value: Boolean?) {
        jsObject.property(name, value)
    }

    override fun property(name: String, value: JsObject?) {
        jsObject.property(name, (value as LiquidCoreJsObject?)?.toJSObject())
    }

    override fun property(name: String, value: JsFunction) {
        jsObject.property(name, functionConverter.convert(value))
    }

    override fun stringListProperty(name: String, value: List<String>) {
        jsObject.property(name, value)
    }

    override fun objectListProperty(name: String, value: List<JsObject>) {
        jsObject.property(name, value.map { (it as LiquidCoreJsObject).toJSObject() })
    }

    fun build(): JsObject =
        LiquidCoreJsObject(jsObject)
}
