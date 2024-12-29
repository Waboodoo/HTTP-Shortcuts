package ch.rmy.android.scripting

import ch.rmy.android.scripting.liquidcore.LiquidCoreScriptingEngine

object ScriptingEngineFactory {
    fun create(): ScriptingEngine =
        LiquidCoreScriptingEngine()
}
