package com.neostra.android.oobe.helper;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.view.View;

public class StatusBarHelper {
    private static StatusBarManager mStatusBar;
    public static void disableStatusBar(Context context) {
        if (mStatusBar == null) {
            mStatusBar = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        mStatusBar.disable(61145088);
    }

    public static void enableStatusBar(Context context) {
        if (mStatusBar != null)
            mStatusBar.disable(StatusBarManager.DISABLE_NONE);
    }

    public static void setFullScreen(Activity activity) {
        activity.getWindow().getDecorView()
        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    public static void setViewImmersive(View v) {
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
}