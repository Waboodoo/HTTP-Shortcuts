package ch.rmy.android.http_shortcuts.dialogs

import android.app.Dialog

import ch.rmy.android.http_shortcuts.utils.Destroyable

class HelpDialog constructor(private val dialog: Dialog) : Destroyable {

    fun show() {
        dialog.show()
    }

    override fun destroy() {
        dialog.dismiss()
    }
}
