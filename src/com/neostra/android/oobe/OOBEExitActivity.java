package com.neostra.android.oobe;

import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.helper.StatusBarHelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class OOBEExitActivity extends Activity {
    private static final String TAG = Define.getTag(OOBEExitActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exit();
    }

    private void exit() {
        Log.d(TAG, "exit()");
        boolean completeStatus = Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
        boolean provisionedStatus = Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Log.d(TAG, "Set user_setup_complete=1, status: " + completeStatus);
        Log.d(TAG, "Set device_provisioned=1, status: " + provisionedStatus);

        Log.d(TAG, "disableSelfAndLaunchHome()");
        StatusBarHelper.enableStatusBar(this);
        PackageManager pm = getPackageManager();
        ComponentName localComponentName = new ComponentName("com.neostra.android.oobe", "com.neostra.android.oobe.wizard.WizardManager");
        pm.setComponentEnabledSetting(localComponentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finishAffinity();
        overridePendingTransition(R.anim.suw_slide_next_in, R.anim.suw_slide_next_out);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}