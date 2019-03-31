package ch.rmy.android.http_shortcuts.activities

import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.drawable
import ch.rmy.android.http_shortcuts.realm.RealmFactory
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.ThemeHelper

abstract class BaseActivity : AppCompatActivity() {

    internal var toolbar: Toolbar? = null

    val destroyer = Destroyer()

    private val themeHelper by lazy {
        ThemeHelper(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmFactory.init(applicationContext)
    }

    override fun setContentView(layoutResID: Int) {
        setTheme(themeHelper.theme)
        super.setContentView(layoutResID)
        toolbar = findViewById<Toolbar?>(R.id.toolbar) ?: return
        updateStatusBarColor()
        setSupportActionBar(toolbar)
        if (navigateUpIcon != 0) {
            enableNavigateUpButton(navigateUpIcon)
        }
    }

    protected val context: Context
        get() = this

    protected open val navigateUpIcon = R.drawable.up_arrow

    private fun enableNavigateUpButton(iconResource: Int) {
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        val upArrow = drawable(context, iconResource) ?: return
        upArrow.setColorFilter(color(context, android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        actionBar.setHomeAsUpIndicator(upArrow)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> consume { onBackPressed() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun updateStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                statusBarColor = themeHelper.statusBarColor
            }
        }
    }

    protected open fun finishWithoutAnimation() {
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyer.destroy()
    }
}
