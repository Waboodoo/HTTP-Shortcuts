package ch.rmy.android.http_shortcuts.scripting

class ResultHandler {

    private var result: String? = null

    fun getResult(): String? =
        result

    fun setResult(result: String) {
        this.result = result
    }
}
