package ch.rmy.android.http_shortcuts.dialogs

import android.app.Dialog

import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.showIfPossible

class HelpDialog constructor(private val dialog: Dialog) {

    fun show(): Destroyable {
        dialog.showIfPossible()
        return object : Destroyable {
            override fun destroy() {
                dialog.dismiss()
            }
        }
    }

}
