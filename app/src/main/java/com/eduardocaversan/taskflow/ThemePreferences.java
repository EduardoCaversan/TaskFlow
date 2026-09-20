package com.eduardocaversan.taskflow;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemePreferences {
    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    private static final String PREFERENCES_NAME = "taskflow_preferences";
    private static final String KEY_THEME_MODE = "theme_mode";

    private ThemePreferences() { }

    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(getThemeMode(context)));
    }

    public static void saveThemeMode(Context context, String mode) {
        preferences(context).edit().putString(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(toNightMode(mode));
    }

    public static String getThemeMode(Context context) {
        return preferences(context).getString(KEY_THEME_MODE, MODE_SYSTEM);
    }

    public static int toSelection(String mode) {
        if (MODE_LIGHT.equals(mode)) return 1;
        if (MODE_DARK.equals(mode)) return 2;
        return 0;
    }

    public static String fromSelection(int selection) {
        if (selection == 1) return MODE_LIGHT;
        if (selection == 2) return MODE_DARK;
        return MODE_SYSTEM;
    }

    private static int toNightMode(String mode) {
        if (MODE_LIGHT.equals(mode)) return AppCompatDelegate.MODE_NIGHT_NO;
        if (MODE_DARK.equals(mode)) return AppCompatDelegate.MODE_NIGHT_YES;
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
