package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import butterknife.ButterKnife;

@SuppressLint("InflateParams")
public class ChangeLogDialog {

    private static final String PREFERENCES_NAME = "change_log";
    private static final String KEY_PERMANENTLY_HIDDEN = "permanently_hidden";
    private static final String KEY_LAST_VERSION = "last_version";

    private final Context context;
    private final SharedPreferences preferences;
    private final boolean whatsNew;

    @Bind(R.id.changelog_text)
    TextView text;
    @Bind(R.id.checkbox_show_at_startup)
    CheckBox showAtStartupCheckbox;

    public ChangeLogDialog(Context context, boolean whatsNew) {
        this.context = context;
        this.whatsNew = whatsNew;

        preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);
    }

    public boolean isPermanentlyHidden() {
        return preferences.getBoolean(KEY_PERMANENTLY_HIDDEN, false);
    }

    public boolean wasAlreadyShown() {
        return getVersion() <= preferences.getInt(KEY_LAST_VERSION, Integer.MAX_VALUE);
    }

    public void show() {
        Editor editor = preferences.edit();
        editor.putInt(KEY_LAST_VERSION, getVersion());
        editor.commit();

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.changelog_dialog, null);
        ButterKnife.bind(this, view);

        new MaterialDialog.Builder(context)
                .customView(view, false)
                .title(whatsNew ? R.string.changelog_title_whats_new : R.string.changelog_title)
                .positiveText(android.R.string.ok)
                .show();
        text.setText(Html.fromHtml(context.getString(R.string.changelog_text)));

        showAtStartupCheckbox.setChecked(!isPermanentlyHidden());
        showAtStartupCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor editor = preferences.edit();
                editor.putBoolean(KEY_PERMANENTLY_HIDDEN, !isChecked);
                editor.commit();
            }

        });

    }

    private int getVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

}
