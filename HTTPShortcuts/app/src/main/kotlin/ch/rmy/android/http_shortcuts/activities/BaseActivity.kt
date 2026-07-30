package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import ch.rmy.android.framework.ui.BaseActivity
import ch.rmy.android.http_shortcuts.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.utils.ActivityProvider

abstract class BaseActivity : BaseActivity() {

    open val initializeWithTheme: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(if (initializeWithTheme) R.style.LightTheme else R.style.Theme_Transparent)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Application.unmigratedRealmFound && this !is MainActivity) {
            finish()
            return
        }

        onCreated(savedInstanceState)
    }

    protected open fun onCreated(savedState: Bundle?) {
        // intentionally left blank
    }

    override fun onStart() {
        super.onStart()
        ActivityProvider.registerActivity(this)
    }

    override fun onStop() {
        super.onStop()
        ActivityProvider.deregisterActivity(this)
    }
}
