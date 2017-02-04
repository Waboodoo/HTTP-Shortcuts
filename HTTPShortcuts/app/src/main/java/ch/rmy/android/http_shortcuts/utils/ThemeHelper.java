package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import ch.rmy.android.http_shortcuts.R;

public class ThemeHelper {

    private final int theme;
    private final int statusBarColor;

    public ThemeHelper(Context context) {
        String themeId = new Settings(context).getTheme();

        switch (themeId) {
            case Settings.THEME_GREEN: {
                theme = R.style.LightThemeAlt1;
                statusBarColor = getColor(context, R.color.primary_dark_alt1);
                break;
            }
            case Settings.THEME_RED: {
                theme = R.style.LightThemeAlt2;
                statusBarColor = getColor(context, R.color.primary_dark_alt2);
                break;
            }
            case Settings.THEME_PURPLE: {
                theme = R.style.LightThemeAlt3;
                statusBarColor = getColor(context, R.color.primary_dark_alt3);
                break;
            }
            case Settings.THEME_GREY: {
                theme = R.style.LightThemeAlt4;
                statusBarColor = getColor(context, R.color.primary_dark_alt4);
                break;
            }
            case Settings.THEME_ORANGE: {
                theme = R.style.LightThemeAlt5;
                statusBarColor = getColor(context, R.color.primary_dark_alt5);
                break;
            }
            case Settings.THEME_INDIGO: {
                theme = R.style.LightThemeAlt6;
                statusBarColor = getColor(context, R.color.primary_dark_alt6);
                break;
            }
            default: {
                theme = R.style.LightTheme;
                statusBarColor = getColor(context, R.color.primary_dark);
                break;
            }
        }
    }

    @ColorInt
    private static int getColor(Context context, @ColorRes int colorRes) {
        return context.getResources().getColor(colorRes);
    }

    public int getTheme() {
        return theme;
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

}
