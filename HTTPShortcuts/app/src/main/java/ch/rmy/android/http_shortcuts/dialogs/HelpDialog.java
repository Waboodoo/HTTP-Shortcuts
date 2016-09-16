package ch.rmy.android.http_shortcuts.dialogs;

import android.app.Dialog;

import ch.rmy.android.http_shortcuts.utils.Destroyable;

public class HelpDialog implements Destroyable {

    private final Dialog dialog;

    protected HelpDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void destroy() {
        dialog.dismiss();
    }
}
