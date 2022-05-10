package ch.rmy.android.framework.ui

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.drawable
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.extensions.setTintCompat
import ch.rmy.android.framework.extensions.showIfPossible
import ch.rmy.android.framework.extensions.showSnackbar
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.utils.Destroyer
import ch.rmy.android.framework.utils.SnackbarManager
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R

abstract class BaseActivity : AppCompatActivity() {

    internal var toolbar: Toolbar? = null

    val destroyer = Destroyer()

    val baseView: ViewGroup?
        get() = (findViewById<ViewGroup>(android.R.id.content))?.getChildAt(0) as ViewGroup?

    protected open val supportsSnackbars = true

    private var currentDialogState: DialogState? = null
    private var currentDialog: Dialog? = null
    private var savedDialogState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedDialogState = savedInstanceState?.getBundle(EXTRA_DIALOG_INSTANCE_STATE)
    }

    override fun onStart() {
        super.onStart()
        if (supportsSnackbars) {
            SnackbarManager.showEnqueuedSnackbars(this)
        }
    }

    fun <T : ViewBinding> applyBinding(binding: T): T =
        binding.also {
            setContentView(binding.root)
            setUpCommonViews()
        }

    private fun setUpCommonViews() {
        baseView?.setBackgroundColor(color(context, R.color.activity_background))
        toolbar = findViewById(R.id.toolbar) ?: return
        updateStatusBarColor()
        setSupportActionBar(toolbar)
        if (navigateUpIcon != 0) {
            enableNavigateUpButton(navigateUpIcon)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setUpCommonViews()
    }

    val context: Context
        get() = this

    protected open val navigateUpIcon = R.drawable.up_arrow

    private fun enableNavigateUpButton(iconResource: Int) {
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        val upArrow = drawable(context, iconResource) ?: return
        upArrow.setTintCompat(Color.WHITE)
        actionBar.setHomeAsUpIndicator(upArrow)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> consume { onBackPressed() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun updateStatusBarColor() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            statusBarColor = computeStatusBarColor()
        }
    }

    protected abstract fun computeStatusBarColor(): Int

    open fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.OpenActivity -> {
                logInfo("handleEvent: Opening activity for ${event.intentBuilder}")
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
            is ViewModelEvent.Finish -> {
                logInfo("handleEvent: Finishing (result=${event.result}, skipAnimation=${event.skipAnimation})")
                if (event.result != null) {
                    setResult(event.result, event.intent)
                }
                if (event.skipAnimation) {
                    finishWithoutAnimation()
                } else {
                    finish()
                }
            }
            is ViewModelEvent.SetResult -> {
                logInfo("handleEvent: Setting result (result=${event.result})")
                setResult(event.result, event.intent)
            }
            is ViewModelEvent.ShowSnackbar -> {
                showSnackbar(event.message.localize(context), long = event.long)
            }
            is ViewModelEvent.ShowToast -> {
                showToast(event.message.localize(context).toString(), long = event.long)
            }
            else -> logException(IllegalArgumentException("Unhandled event: $event"))
        }
    }

    protected fun setTitle(title: Localizable?) {
        setTitle(title?.localize(context))
    }

    protected fun setSubtitle(subtitle: Localizable?) {
        toolbar?.setSubtitle(subtitle)
    }

    protected fun setDialogState(dialogState: DialogState?, viewModel: WithDialog) {
        if (currentDialogState == dialogState && !(currentDialog == null && currentDialogState != null)) {
            return
        }
        logInfo("Setting dialog state: ${dialogState?.let { it.id ?: "no-id" } ?: "no dialog"}")
        currentDialog?.dismiss()
        currentDialogState = dialogState
        currentDialog = dialogState?.createDialog(context, viewModel)
        currentDialog?.showIfPossible()
        currentDialog?.let { dialog ->
            savedDialogState?.let { dialogState ->
                currentDialogState?.restoreInstanceState(dialog, dialogState)
            }
        }
        savedDialogState = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentDialog?.let { dialog ->
            currentDialogState?.let { dialogState ->
                outState.putBundle(EXTRA_DIALOG_INSTANCE_STATE, dialogState.saveInstanceState(dialog))
            }
        }
        currentDialog?.setOnDismissListener(null)
        currentDialog?.dismiss()
        currentDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyer.destroy()
    }

    companion object {
        private const val EXTRA_DIALOG_INSTANCE_STATE = "dialog-instance-state"
    }
}
