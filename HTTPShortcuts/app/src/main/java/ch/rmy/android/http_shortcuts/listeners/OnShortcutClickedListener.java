package ch.rmy.android.http_shortcuts.listeners;

import android.view.View;

import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;

public interface OnShortcutClickedListener {

    void onShortcutClicked(Shortcut shortcut, View view);

    void onShortcutLongClicked(Shortcut shortcut, View view);

}
