package ch.rmy.android.scripting

class ScriptingException
internal constructor(
    override val cause: Exception?,
    override val message: String? = null,
    val lineNumber: Int? = null,
) : Exception(message, cause)
