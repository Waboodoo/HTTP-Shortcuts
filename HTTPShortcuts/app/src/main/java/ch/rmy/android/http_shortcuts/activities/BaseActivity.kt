package ch.rmy.android.http_shortcuts.activities

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.ViewGroup
import android.view.WindowManager
import butterknife.ButterKnife
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.ThemeHelper
import ch.rmy.android.http_shortcuts.utils.UIUtil

abstract class BaseActivity : AppCompatActivity() {

    internal var toolbar: Toolbar? = null

    protected val destroyer = Destroyer()
    private var themeHelper: ThemeHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.themeHelper = ThemeHelper(context)
    }

    override fun setContentView(layoutResID: Int) {
        setTheme(themeHelper!!.theme)
        super.setContentView(layoutResID)
        bindViews()
        toolbar = findViewById(R.id.toolbar) as Toolbar? ?: return
        updateStatusBarColor()
        setSupportActionBar(toolbar)
        if (navigateUpIcon != 0) {
            enableNavigateUpButton(navigateUpIcon)
        }
    }

    open fun bindViews() { // TODO: Remove this once everything is migrated to Kotlin
        ButterKnife.bind(this);
    }

    protected val context = this

    protected open val navigateUpIcon = R.drawable.up_arrow

    private fun enableNavigateUpButton(iconResource: Int) {
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        val upArrow = UIUtil.getDrawable(context, iconResource) ?: return
        upArrow.setColorFilter(UIUtil.getColor(context, android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        actionBar.setHomeAsUpIndicator(upArrow)
    }

    fun showSnackbar(@StringRes message: Int) {
        showSnackbar(getString(message))
    }

    fun showSnackbar(message: CharSequence) {
        val baseView = (findViewById(android.R.id.content) as ViewGroup).getChildAt(0)
        Snackbar.make(baseView, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = themeHelper!!.statusBarColor
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
