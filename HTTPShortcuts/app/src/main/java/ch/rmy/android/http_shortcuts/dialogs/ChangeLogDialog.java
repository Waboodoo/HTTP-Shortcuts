package ch.rmy.android.http_shortcuts.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
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
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.Settings;

public class ChangeLogDialog {

    private final Context context;
    private final Settings settings;
    private final boolean whatsNew;

    @Bind(R.id.changelog_text)
    TextView text;
    @Bind(R.id.checkbox_show_at_startup)
    CheckBox showAtStartupCheckbox;

    public ChangeLogDialog(Context context, boolean whatsNew) {
        this.context = context;
        this.whatsNew = whatsNew;

        settings = new Settings(context);
    }

    public boolean shouldShow() {
        if (isPermanentlyHidden()) {
            return false;
        }
        int lastSeenVersion = settings.getChangeLogLastVersion();
        return getVersion() > lastSeenVersion && lastSeenVersion != 0;
    }

    private boolean isPermanentlyHidden() {
        return settings.isChangeLogPermanentlyHidden();
    }

    @SuppressLint("InflateParams")
    public void show() {
        settings.setChangeLogLastVersion(getVersion());

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
                settings.setChangeLogPermanentlyHidden(!isChecked);
            }

        });
    }

    private int getVersion() {
        try {
            return (context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionCode / 1000) * 1000;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

}
