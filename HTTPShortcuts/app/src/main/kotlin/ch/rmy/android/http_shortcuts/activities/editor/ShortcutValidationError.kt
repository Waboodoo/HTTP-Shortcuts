package ch.rmy.android.http_shortcuts.activities.editor

class ShortcutValidationError(val type: Int) : RuntimeException() {

    override val message: String?
        get() = "Shortcut validation error: $type"

}