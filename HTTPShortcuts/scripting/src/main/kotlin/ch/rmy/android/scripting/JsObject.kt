package ch.rmy.android.scripting

interface JsObjectBuilder {
    fun property(name: String, value: String?)
    fun property(name: String, value: Int?)
    fun property(name: String, value: Long?)
    fun property(name: String, value: Boolean?)
    fun property(name: String, value: JsObject?)
    fun property(name: String, value: JsFunction)
    fun property(name: String, value: List<String>)
    fun function(name: String, function: (JsFunctionArgs) -> Any?) {
        property(
            name,
            object : JsFunction {
                override fun invoke(args: JsFunctionArgs): Any? =
                    function(args)
            }
        )
    }
}

interface JsObject
