package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class MenuDialogBuilder {

    private final MaterialDialog.Builder builder;
    private final List<CharSequence> names = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();

    public MenuDialogBuilder(Context context) {
        builder = new MaterialDialog.Builder(context);
    }

    public MenuDialogBuilder title(@StringRes int title) {
        builder.title(builder.getContext().getString(title));
        return this;
    }

    public MenuDialogBuilder title(CharSequence title) {
        builder.title(title);
        return this;
    }

    public MenuDialogBuilder item(@StringRes int name, Action action) {
        return item(builder.getContext().getString(name), action);
    }

    public MenuDialogBuilder item(CharSequence name, Action action) {
        names.add(name);
        actions.add(action);
        return this;
    }

    public MenuDialogBuilder dismissListener(DialogInterface.OnDismissListener onDismissListener) {
        builder.dismissListener(onDismissListener);
        return this;
    }

    public void show() {
        builder.items(names).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                actions.get(which).execute();
            }
        }).show();
    }

    public interface Action {

        void execute();

    }

}
