package com.neostra.android.oobe;

import com.android.setupwizardlib.util.WizardManagerHelper;
import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.helper.Utility;
import com.neostra.android.oobe.wizard.WizardManager;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.setupwizardlib.util.SystemBarHelper;

public class SetupCompleteActivity extends Activity {
    private static final String TAG = Define.getTag(SetupCompleteActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setup_complete);
    }

    public void goFinish(View v) {
        gotoNext();
    }

    public void gotoNext() {
        if (Utility.hasGMS(this)) {
            Intent i = WizardManagerHelper.getNextIntent(getIntent(), RESULT_OK);
            startActivity(i);
        } else {
            Intent i = WizardManager.getNextWizardIntent(getIntent(), RESULT_OK);
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemBarHelper.hideSystemBars(getWindow());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoNext();
            }
        },
        2000);    //延时2s执行
    }
}