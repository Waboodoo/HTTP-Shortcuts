package ch.rmy.android.http_shortcuts.icons;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.listeners.OnIconSelectedListener;

/**
 * A dialog window that lists all built-in icons, from which the user can select one.
 *
 * @author Roland Meyer
 */
public class IconSelector {

    private final Dialog dialog;

    /**
     * Creates the icon selection dialog.
     *
     * @param context  The context
     * @param listener Used as callback when the user selects an icon.
     */
    public IconSelector(final Context context, final OnIconSelectedListener listener) {
        dialog = (new MaterialDialog.Builder(context))
                .title(R.string.choose_icon)
                .customView(R.layout.dialog_icon_selector, false)
                .build();

        RecyclerView grid = (RecyclerView) dialog.findViewById(R.id.icon_selector_grid);
        grid.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 6);
        grid.setLayoutManager(layoutManager);
        IconAdapter adapter = new IconAdapter(context, new OnIconSelectedListener() {

            @Override
            public void onIconSelected(String iconName) {
                dialog.dismiss();
                listener.onIconSelected(iconName);
            }
        });
        grid.setAdapter(adapter);
    }

    public void show() {
        dialog.show();
    }

}
