package com.neostra.android.oobe;

import java.lang.reflect.Field;
import java.util.Locale;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;

import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePicker.LocaleInfo;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.wizard.WizardManager;

public class WelcomeActivity extends Activity {
    private static final String TAG = Define.getTag(WelcomeActivity.class);
    private boolean isClicked = false;
    private boolean changePicture = false;
    private Button btn;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN); 
        setContentView(R.layout.activity_welcome);
		tv = findViewById(R.id.welcome_agree);
        btn = findViewById(R.id.welcome_select_btn);
    }

	public void changeBtn(View v) {
        isClicked = !isClicked;
        changePicture = !changePicture;
        changBtnPicture();
    }
	
    public void changBtnPicture(){
        if(changePicture){
            btn.setBackgroundResource(R.drawable.ic_welcome_selected);
            tv.setBackgroundColor(0xFFE6333F);
        }else{
            btn.setBackgroundResource(R.drawable.ic_welcome_unselected);
            tv.setBackgroundColor(0xFF999999);
        }
    }
	
    public void agreePolicy(View view){
        if(isClicked){
            //finish();
			Log.d(TAG, "goNext");
            Intent i = WizardManager.getNextWizardIntent(getIntent(), RESULT_OK);
            startActivity(i);
        }
    }
    public void goDetail(View view){
        Intent intent = new Intent(WelcomeActivity.this,DetailActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemBarHelper.hideSystemBars(getWindow());
    }
}
