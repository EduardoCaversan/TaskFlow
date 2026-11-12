package com.eduardocaversan.taskflow;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/** Protege controles das barras, recortes da tela e teclado, inclusive em edge-to-edge. */
final class ScreenInsets {
    private ScreenInsets() { }
    static void apply(Activity activity) {
        // Nas versões anteriores, o decor e adjustResize já reservam essas áreas.
        if (Build.VERSION.SDK_INT < 35) return;
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        int left = root.getPaddingLeft(), top = root.getPaddingTop();
        int right = root.getPaddingRight(), bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            view.setPadding(left + insets.left, top + insets.top, right + insets.right, bottom + insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
