package ch.rmy.android.scripting

interface ScriptingEngine {
    fun setExceptionHandler(onException: (ScriptingException) -> Unit)

    fun evaluateScript(script: String)

    fun registerFunction(name: String, function: JsFunction)

    fun registerObject(name: String, obj: JsObject?)

    fun registerString(name: String, string: String?)

    fun registerListOfObjects(name: String, list: List<JsObject>)

    fun buildJsObject(builder: JsObjectBuilder.() -> Unit): JsObject

    fun destroy()
}
