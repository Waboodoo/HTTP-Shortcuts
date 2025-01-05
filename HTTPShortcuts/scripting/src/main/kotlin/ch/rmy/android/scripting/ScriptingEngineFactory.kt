package ch.rmy.android.scripting

import ch.rmy.android.scripting.quickjs.QuickJsScriptingEngine

object ScriptingEngineFactory {
    fun create(): ScriptingEngine =
        QuickJsScriptingEngine()
}
