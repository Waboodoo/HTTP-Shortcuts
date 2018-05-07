package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.Destroyer

abstract class BaseActionEditorView(context: Context, layoutId: Int) : FrameLayout(context), Destroyable {

    init {
        View.inflate(context, layoutId, this)
    }

    protected val destroyer = Destroyer()

    abstract fun compile(): Boolean

    override fun destroy() {
        destroyer.destroy()
    }

}