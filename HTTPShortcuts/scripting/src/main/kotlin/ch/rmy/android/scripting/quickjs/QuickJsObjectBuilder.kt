package ch.rmy.android.scripting.quickjs

import ch.rmy.android.scripting.JsFunction
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.JsObjectBuilder
import ch.rmy.android.scripting.ScriptingEngine
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext

internal class QuickJsObjectBuilder(
    override val scriptingEngine: ScriptingEngine,
    private val jsContext: QuickJSContext,
    private val functionConverter: QuickJsFunctionConverter,
) : JsObjectBuilder {
    private val jsObject = jsContext.createNewJSObject()

    override fun property(name: String, value: String?) {
        jsObject.setProperty(name, value)
    }

    override fun property(name: String, value: Int?) {
        if (value != null) {
            jsObject.setProperty(name, value)
        } else {
            jsObject.setNull(name)
        }
    }

    private fun JSObject.setNull(name: String) {
        setProperty(name, null as String?)
    }

    override fun property(name: String, value: Long?) {
        if (value != null) {
            jsObject.setProperty(name, value)
        } else {
            jsObject.setNull(name)
        }
    }

    override fun property(name: String, value: Float?) {
        property(name, value?.toDouble())
    }

    override fun property(name: String, value: Double?) {
        if (value != null) {
            jsObject.setProperty(name, value)
        } else {
            jsObject.setNull(name)
        }
    }

    override fun property(name: String, value: Boolean?) {
        if (value != null) {
            jsObject.setProperty(name, value)
        } else {
            jsObject.setNull(name)
        }
    }

    override fun property(name: String, value: JsObject?) {
        jsObject.setProperty(name, (value as QuickJsObject?)?.toJSObject())
    }

    override fun property(name: String, value: JsFunction) {
        jsObject.setProperty(name, functionConverter.convert(value))
    }

    override fun stringListProperty(name: String, value: List<String>) {
        val array = jsContext.createNewJSArray()
        value.forEachIndexed { index, item ->
            array.set(item, index)
        }
        jsObject.setProperty(name, array)
    }

    override fun objectListProperty(name: String, value: List<JsObject>) {
        val array = jsContext.createNewJSArray()
        value.forEachIndexed { index, item ->
            array.set((item as QuickJsObject).toJSObject(), index)
        }
        jsObject.setProperty(name, array)
    }

    fun build(): JsObject =
        QuickJsObject(jsObject)
}
