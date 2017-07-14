package ch.rmy.android.http_shortcuts.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.Destroyer;
import ch.rmy.android.http_shortcuts.utils.ThemeHelper;
import ch.rmy.android.http_shortcuts.utils.UIUtil;

public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    protected final Destroyer destroyer = new Destroyer();
    private ThemeHelper themeHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.themeHelper = new ThemeHelper(getContext());
    }

    @Override
    public void setContentView(int layoutResID) {
        setTheme(themeHelper.getTheme());
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        if (toolbar != null) {
            updateStatusBarColor();
            setSupportActionBar(toolbar);
            if (getNavigateUpIcon() != 0) {
                enableNavigateUpButton(getNavigateUpIcon());
            }
        }
    }

    protected Context getContext() {
        return this;
    }

    protected int getNavigateUpIcon() {
        return R.drawable.up_arrow;
    }

    private void enableNavigateUpButton(int iconResource) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = UIUtil.getDrawable(getContext(), iconResource);
            if (upArrow != null) {
                upArrow.setColorFilter(UIUtil.getColor(getContext(), android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                actionBar.setHomeAsUpIndicator(upArrow);
            }
        }
    }

    public void showSnackbar(@StringRes int message) {
        showSnackbar(getString(message));
    }

    public void showSnackbar(CharSequence message) {
        View baseView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        Snackbar.make(baseView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(themeHelper.getStatusBarColor());
        }
    }

    protected void finishWithoutAnimation() {
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyer.destroy();
    }
}
