package com.eduardocaversan.taskflow;

import android.app.Application;

public class TaskFlowApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemePreferences.applySavedTheme(this);
    }
}
