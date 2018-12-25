package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.DestroyerDestroyable

abstract class BaseActionEditorView(context: Context, layoutId: Int) : FrameLayout(context), DestroyerDestroyable {

    init {
        View.inflate(context, layoutId, this)
    }

    override val destroyer = Destroyer()

    abstract fun compile(): Boolean

}