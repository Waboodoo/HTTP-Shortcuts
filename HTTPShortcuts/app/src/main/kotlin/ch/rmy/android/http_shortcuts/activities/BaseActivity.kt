package ch.rmy.android.http_shortcuts.activities

import android.content.Context
import android.os.Bundle
import ch.rmy.android.framework.ui.BaseActivity
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.ThemeHelper

abstract class BaseActivity : BaseActivity() {

    val themeHelper by lazy {
        ThemeHelper(context)
    }

    open val initializeWithTheme: Boolean
        get() = true

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(base))
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        if (initializeWithTheme) {
            setTheme(themeHelper.theme)
        }
        super.onCreate(savedInstanceState)
        try {
            RealmFactory.init(applicationContext)
            onCreated(savedInstanceState)
        } catch (e: RealmFactory.RealmNotFoundException) {
            if (this is Entrypoint) {
                showRealmError()
            } else {
                throw e
            }
        }
    }

    protected open fun onCreated(savedState: Bundle?) {
        // intentionally left blank
    }

    private fun showRealmError() {
        DialogBuilder(context)
            .title(R.string.dialog_title_error)
            .message(R.string.error_realm_unavailable, isHtml = true)
            .positive(R.string.dialog_ok)
            .dismissListener {
                finish()
            }
            .showIfPossible()
    }

    override fun computeStatusBarColor(): Int =
        themeHelper.statusBarColor
}
