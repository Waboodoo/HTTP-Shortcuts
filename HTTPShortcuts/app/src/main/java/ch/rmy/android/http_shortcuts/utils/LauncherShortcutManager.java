package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.bugsnag.android.Bugsnag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class LauncherShortcutManager {

    private static final String ID_PREFIX = "shortcut_";

    public static void updateAppShortcuts(Context context, Collection<Category> categories) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            update(context, categories);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private static void update(Context context, Collection<Category> categories) {
        try {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            int max;
            try {
                max = shortcutManager.getMaxShortcutCountPerActivity();
            } catch (Exception e) {
                Bugsnag.notify(e);
                max = 5;
            }

            List<ShortcutInfo> launcherShortcuts = createLauncherShortcuts(context, categories, max);

            shortcutManager.setDynamicShortcuts(launcherShortcuts);
        } catch(Exception e) {
            Bugsnag.notify(e);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private static List<ShortcutInfo> createLauncherShortcuts(Context context, Collection<Category> categories, int max) {
        int count = 0;
        List<ShortcutInfo> launcherShortcuts = new ArrayList<>();
        for (Category category : categories) {
            for (Shortcut shortcut : category.getShortcuts()) {
                if (shortcut.isLauncherShortcut()) {
                    int rank = max - count + 1;
                    launcherShortcuts.add(createShortcutInfo(context, shortcut, rank));
                    if (++count >= max) {
                        return launcherShortcuts;
                    }
                }
            }
        }
        return launcherShortcuts;
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private static ShortcutInfo createShortcutInfo(Context context, Shortcut shortcut, int rank) {
        ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, ID_PREFIX + shortcut.getId())
                .setShortLabel(shortcut.getName())
                .setLongLabel(shortcut.getName())
                .setRank(rank)
                .setIntent(IntentUtil.createIntent(context, shortcut.getId()));
        Icon icon = shortcut.getIcon(context);
        if (icon != null) {
            builder = builder.setIcon(icon);
        }
        return builder.build();
    }

}
