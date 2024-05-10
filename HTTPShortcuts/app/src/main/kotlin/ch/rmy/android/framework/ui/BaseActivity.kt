package ch.rmy.android.framework.ui

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.utils.SnackbarManager
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R

abstract class BaseActivity : AppCompatActivity() {

    val context: Context
        get() = this

    open fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.SendIntent -> {
                logInfo("handleEvent: sending intent")
                try {
                    event.intentBuilder.startActivity(this)
                } catch (e: ActivityNotFoundException) {
                    showToast(R.string.error_not_supported)
                }
            }
            is ViewModelEvent.SendBroadcast -> {
                sendBroadcast(event.intent)
            }
            is ViewModelEvent.OpenURL -> {
                logInfo("handleEvent: Opening URL ${event.url}")
                openURL(event.url)
            }
            is ViewModelEvent.CloseScreen -> {
                logInfo("handleEvent: closing screen")
                finishWithoutAnimation()
            }
            is ViewModelEvent.Finish -> {
                logInfo("handleEvent: Finishing (resultCode=${event.resultCode}, skipAnimation=${event.skipAnimation})")
                if (event.resultCode != null) {
                    setResult(event.resultCode, event.intent)
                }
                if (event.skipAnimation) {
                    finishWithoutAnimation()
                } else {
                    finish()
                }
            }
            is ViewModelEvent.SetActivityResult -> {
                logInfo("handleEvent: Setting result (result=${event.result})")
                setResult(event.result, event.intent)
            }
            is ViewModelEvent.ShowSnackbar -> {
                SnackbarManager.enqueueSnackbar(event.message.localize(context).toString(), long = event.long)
            }
            is ViewModelEvent.ShowToast -> {
                showToast(event.message.localize(context).toString(), long = event.long)
            }
            else -> {
                showToast(R.string.error_generic)
                logException(IllegalArgumentException("Unhandled event: $event"))
            }
        }
    }
}
