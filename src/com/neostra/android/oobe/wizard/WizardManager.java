package com.neostra.android.oobe.wizard;

import com.android.setupwizardlib.util.WizardManagerHelper;
import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.helper.StatusBarHelper;
import com.neostra.android.oobe.helper.UserHelper;
import com.neostra.android.oobe.helper.Utility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

/** Handle oobe flow */
public class WizardManager extends Activity {
    private static final String TAG               = Define.getTag(WizardManager.class);
    private static WizardParser wizardParser;

    public static final String EXTRA_ACTION_ID    = "actionId";
    public static final String EXTRA_RESULT_CODE  = "com.android.setupwizard.ResultCode";
    public static final String OOBE_NEXT_ACTION   = "com.neostra.android.oobe.NEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ("ENABLE".equals(SystemProperties.get("ro.demo.mode", "DISABLED")) ||
                UserHelper.isRestricted(this) ||
                UserHelper.isGuest(this) ||
                (WizardManagerHelper.isUserSetupComplete(this) &&
                 WizardManagerHelper.isDeviceProvisioned(this))) {
            Log.d(TAG, "Is demo mode, restricted/guest user or setup complete, finish oobe");
            exit();
            return ;
        }

        String action = getIntent().getAction();
        Log.d(TAG, "Action: " + action);

        String nextActionId = null;
        if (OOBE_NEXT_ACTION.equals(action)) {
            String actionId = getIntent().getStringExtra(EXTRA_ACTION_ID);
            int resultCode = getIntent().getIntExtra(EXTRA_RESULT_CODE, RESULT_OK);
            if (wizardParser == null) wizardParser = new WizardParser(this);
            nextActionId = findNextActionId(actionId, resultCode);
        } else {
            Log.d(TAG, "Init wizard script and start the first action");
            StatusBarHelper.disableStatusBar(this);
            wizardParser = new WizardParser(this);
            nextActionId = wizardParser.firstAction;
        }

        if (!TextUtils.isEmpty(nextActionId)) {
            startWithActionId(nextActionId);
            setResult(Define.RESULT_BACK);
            finish();
        } else {
            exit();
        }
    }

    private void exit() {
        Log.d(TAG, "exit()");
        Intent exitIntent = new Intent("com.neostra.android.oobe.SETUP_EXIT");
        exitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(exitIntent);
        finish();
    }

    private void startWithActionId(String actionId) {
        try {
            String actionUri = getActionUri(actionId);
            Log.d(TAG, "StartWithActionId: " + actionId + ", actionUri: " + actionUri);

            Intent intent = Intent.parseUri(actionUri, Intent.URI_INTENT_SCHEME);
            intent.putExtra(EXTRA_ACTION_ID, actionId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (Utility.isIntentExist(this, intent))
                startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "parseUri actionId: " + actionId + " err", e);
        }
    }

    private String findNextActionId(String nowActionId, int resultCode) {
        Log.d(TAG, "FindNextActionId nowActionId: " + nowActionId + ", resultCode: " + resultCode);
        WizardAction action = wizardParser.map.get(nowActionId);
        if (action == null) {
            Log.e(TAG, "FindNextActionId not found wizardAction with " + nowActionId);
            return "";
        }
        String nextActionId = action.resultArr.get(resultCode);
        if (TextUtils.isEmpty(nextActionId)) {
            nextActionId = action.defaultAction;
        }
        return nextActionId;
    }

    private String getActionUri(String actionId) {
        WizardAction action = wizardParser.map.get(actionId);
        if (action == null) {
            Log.e(TAG, "GetActionUri not found wizardAction with " + actionId);
            return "";
        }
        return action.uri;
    }

    public static Intent getNextWizardIntent(Intent i, int resultCode) {
        Intent intent = new Intent(OOBE_NEXT_ACTION);
        intent.putExtra(EXTRA_ACTION_ID, i.getStringExtra(EXTRA_ACTION_ID));
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);

        return intent;
    }
}