package ch.rmy.android.scripting

interface JsFunction {
    operator fun invoke(args: JsFunctionArgs): Any?
}
