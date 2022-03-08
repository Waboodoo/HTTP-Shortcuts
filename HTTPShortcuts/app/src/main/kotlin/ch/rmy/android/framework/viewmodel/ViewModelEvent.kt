package ch.rmy.android.framework.viewmodel

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable

abstract class ViewModelEvent {
    data class Finish(
        val result: Int? = null,
        val intent: Intent? = null,
        val skipAnimation: Boolean = false,
    ) : ViewModelEvent()

    data class SetResult(
        val result: Int,
        val intent: Intent? = null,
    ) : ViewModelEvent()

    data class OpenURL(val url: String) : ViewModelEvent()

    data class OpenActivity(
        val intentBuilder: BaseIntentBuilder,
        val requestCode: Int? = null,
    ) : ViewModelEvent()

    data class SendBroadcast(
        val intent: Intent,
    ) : ViewModelEvent()

    @Deprecated("Use DialogState instead")
    class ShowDialog(val dialogBuilder: (context: Context) -> Dialog?) : ViewModelEvent()

    class ShowSnackbar(
        val message: Localizable,
        val long: Boolean = false,
    ) : ViewModelEvent() {
        constructor(@StringRes message: Int, long: Boolean = false) : this(StringResLocalizable(message, long))
    }

    class ShowToast(
        val message: Localizable,
        val long: Boolean = false,
    ) : ViewModelEvent() {
        constructor(@StringRes message: Int, long: Boolean = false) : this(StringResLocalizable(message, long))
    }
}
