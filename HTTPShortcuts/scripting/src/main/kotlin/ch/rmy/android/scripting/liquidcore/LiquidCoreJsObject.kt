package ch.rmy.android.scripting.liquidcore

import ch.rmy.android.scripting.JsObject
import org.liquidplayer.javascript.JSObject

internal class LiquidCoreJsObject(
    private val jsObject: JSObject,
) : JsObject {
    fun toJSObject(): JSObject =
        jsObject
}
