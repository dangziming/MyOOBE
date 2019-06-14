package com.neostra.android.oobe.helper;

import com.neostra.android.oobe.wizard.WizardManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferencesUtils {
    public static final String SHAREDPREFERENCE_NAME         = "oobe_shared";

    /* dataConnectionChooserResult show the user-selection on DataConnectionChooser
     *  -1: no selection (The page is not shown)
     *  0: both 3g and wifi connection
     *  1: use wifi only
     *  2: set up later
     */
    public static final String DATA_CONNECTION_RESULT_KEY    = "dataConnectionChooserResult";

    public static final int DEFAULT_DATA_CONNECTION_RESULT   = Define.DATA_CONNECTION_BOTH;

    /* use to save the intent com.android.setupwizard.OEM_POST_SETUP from google setupwizard */
    public static final String INTENT_SCRIPT_URI             = "scriptUri";
    public static final String INTENT_ACTION_ID              = "actionId";
    public static final String INTENT_THEME                  = "theme";
    public static final String INTENT_WIZARD_ACTION_ID       = "wizard_action_id";

    public static int getIntVal(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

    public static void setIntVal(Context context, String key, int val) {
        SharedPreferences sp = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public static String getStringVal(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    public static void setStringVal(Context context, String key, String val) {
        SharedPreferences sp = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static void saveGoogleSetupWizardIntent(Context context, Intent i) {
        SharedPreferences sp = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String scriptUri = i.getStringExtra(INTENT_SCRIPT_URI);
        String actionId = i.getStringExtra(INTENT_ACTION_ID);
        String theme = i.getStringExtra(INTENT_THEME);

        editor.putString(INTENT_SCRIPT_URI, scriptUri);
        editor.putString(INTENT_ACTION_ID, actionId);
        editor.putString(INTENT_THEME, theme);

        Log.d(Define.getTag(SharedPreferencesUtils.class), "Save GoogleSetupWizardIntent scriptUri=" + scriptUri + ", actionId=" + actionId +
                ", theme=" + theme);
        editor.commit();
    }

    public static void applyGoogleIntent(Context context, Intent i) {
        SharedPreferences sp = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        i.putExtra(INTENT_SCRIPT_URI, sp.getString(INTENT_SCRIPT_URI, ""));
        i.putExtra(INTENT_ACTION_ID, sp.getString(INTENT_ACTION_ID, ""));
        i.putExtra(INTENT_THEME, sp.getString(INTENT_THEME, ""));
    }

    public static void saveWizardIntent(Context context, Intent i) {
        SharedPreferences sp = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        String actionId = i.getStringExtra(WizardManager.EXTRA_ACTION_ID);

        editor.putString(INTENT_WIZARD_ACTION_ID, actionId);

        Log.d(Define.getTag(SharedPreferencesUtils.class), "Save WizardIntent actionId=" + actionId);
        editor.commit();
    }
}