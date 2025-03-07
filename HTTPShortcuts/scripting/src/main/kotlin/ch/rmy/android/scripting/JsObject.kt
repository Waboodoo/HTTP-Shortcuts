package ch.rmy.android.scripting

interface JsObjectBuilder {
    val scriptingEngine: ScriptingEngine

    fun property(name: String, value: String?)
    fun property(name: String, value: Int?)
    fun property(name: String, value: Long?)
    fun property(name: String, value: Float?)
    fun property(name: String, value: Double?)
    fun property(name: String, value: Boolean?)
    fun property(name: String, value: JsObject?)
    fun property(name: String, value: JsFunction)
    fun stringListProperty(name: String, value: List<String>)
    fun objectListProperty(name: String, value: List<JsObject>)
    fun function(name: String, function: (JsFunctionArgs) -> Any?) {
        property(
            name,
            object : JsFunction {
                override fun invoke(args: JsFunctionArgs): Any? =
                    function(args)
            },
        )
    }

    fun objectProperty(name: String, builder: JsObjectBuilder.() -> Unit) {
        property(name, scriptingEngine.buildJsObject(builder))
    }

    fun property(name: String, value: Map<String, List<String>>) {
        property(name, value.toJsObject())
    }

    private fun Map<String, List<String>>.toJsObject(): JsObject =
        scriptingEngine.buildJsObject {
            forEach { (key, values) ->
                stringListProperty(key, values)
            }
        }
}

interface JsObject
