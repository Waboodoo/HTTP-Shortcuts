package ch.rmy.android.scripting.quickjs

import ch.rmy.android.scripting.JsObject
import com.whl.quickjs.wrapper.JSObject

internal class QuickJsObject(
    private val jsObject: JSObject,
) : JsObject {
    fun toJSObject(): JSObject =
        jsObject
}
