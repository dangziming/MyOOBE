package com.neostra.android.oobe;

import java.util.LinkedList;
import java.util.List;

import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.helper.SharedPreferencesUtils;
import com.neostra.android.oobe.helper.Utility;
import com.neostra.android.oobe.wizard.WizardManager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

public class OEMPostSetupActivity extends Activity {
    private static final String TAG = Define.getTag(OEMPostSetupActivity.class);
    private final static String ACTION_POST_SETUP  = "com.android.setupwizard.OEM_POST_SETUP";
    private final static String ACTION_GOOGLE_NEXT = "com.android.wizard.NEXT";
    private final static String ACTION_FLOW        = "com.neostra.android.oobe.FLOW2";
    public final static String ACTION_NEXT         = "com.neostra.android.oobe.POST_NEXT";

    private final static String EXTRA_PRIORITY    = "priority";
    private final static int MAX_PRIORITY         = 1000;

    private static List<ResolveInfo> infoList = new LinkedList<ResolveInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Intent nextIntent = null;
        if (ACTION_POST_SETUP.equals(intent.getAction())) {
            // init resolveinfo list, and start the first activity
            initInfoList();
            Log.d(TAG, ACTION_POST_SETUP + " init infoList size: " + infoList.size());
            if (Utility.hasGMS(this)) {
                Log.d(TAG, "Is call from google setupwizard and save intent value");
                SharedPreferencesUtils.saveGoogleSetupWizardIntent(this, intent);
            } else {
                Log.d(TAG, "Not from google setupwizard, save intent value");
                SharedPreferencesUtils.saveWizardIntent(this, intent);
            }
            nextIntent = getNextIntent(MAX_PRIORITY);
        } else {
            Log.d(TAG, ACTION_NEXT + " with priority: " + intent.getIntExtra(EXTRA_PRIORITY, -1));
            nextIntent = getNextIntent(intent.getIntExtra(EXTRA_PRIORITY, -1));
        }

        Log.d(TAG, "StartNext: " + nextIntent);
        if (nextIntent == null) {
            exit();
        } else {
            startActivity(nextIntent);
            setResult(Define.RESULT_BACK);
            finish();
        }
    }

    private void initInfoList() {
        Intent i = new Intent(ACTION_FLOW);
        infoList = getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
    }

    private Intent getNextIntent(int priority) {
        if (infoList.isEmpty()) initInfoList();

        for (ResolveInfo info : infoList) {
            if (info.priority < priority) {
                ActivityInfo ai = info.activityInfo;
                Intent i = new Intent(ACTION_FLOW);
                i.setClassName(ai.packageName, ai.name);
                i.putExtra(EXTRA_PRIORITY, info.priority);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Log.i(TAG, "packageName: " + ai.packageName + "; priority: " + info.priority);

                // check if intent exist
                if (!Utility.isIntentExist(this, i))
                    continue;

                return i;
            }
        }

        return null;
    }

    private void exit() {
        Log.d(TAG, "exit()");
        if (Utility.hasGMS(this)) {
            // from google setupwizard, send the google intent
            Intent intent = new Intent(ACTION_GOOGLE_NEXT);
            SharedPreferencesUtils.applyGoogleIntent(this, intent);
            intent.putExtra("com.android.setupwizard.ResultCode", RESULT_OK);
            Log.d(TAG, "Is from google setupwizard send intent: " + intent);
            startActivity(intent);
            finish();
        } else {
            Intent i = new Intent(WizardManager.OOBE_NEXT_ACTION);
            i.putExtra(WizardManager.EXTRA_ACTION_ID,
                    SharedPreferencesUtils.getStringVal(this, SharedPreferencesUtils.INTENT_WIZARD_ACTION_ID, "oem_post_setup"));
            i.putExtra(WizardManager.EXTRA_RESULT_CODE, RESULT_OK);
            Log.d(TAG, "Not from google setupwizard send intent: " + i);
            startActivity(i);
            finish();
        }
    }
}