package ch.rmy.android.http_shortcuts.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.Settings;

public class IconNameChangeDialog {

    private final Context context;
    private final Settings settings;

    public IconNameChangeDialog(Context context) {
        this.context = context;

        settings = new Settings(context);
    }

    public void show(@NonNull MaterialDialog.SingleButtonCallback callback) {
        Dialog dialog = (new MaterialDialog.Builder(context))
                .positiveText(R.string.dialog_ok)
                .onPositive(callback)
                .customView(R.layout.dialog_icon_name_changes, true)
                .build();

        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.checkbox_do_not_show_again);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setIconNameWarningPermanentlyHidden(isChecked);
            }
        });

        dialog.show();
    }

    public boolean shouldShow() {
        return !settings.isIconNameWarningPermanentlyHidden();
    }

}
