package com.neostra.android.oobe.helper;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.neostra.android.oobe.R;


public class PasswordDialog extends Dialog {

	private Context context;
	private String title;
	private EditText etPassword;
	private ImageView showPassword;
	private boolean show;
	private ClickListenerInterface clickListenerInterface;

	public interface ClickListenerInterface {

		public void doConfirm(String title, String password);

		public void doCancel();
	}

	public PasswordDialog(Context context, String title) {
		super(context);
		this.context = context;
		this.title = title;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		init();
	}

	public void init() {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.dialog_password, null);
		setContentView(view);

		TextView tvTitle = (TextView) view.findViewById(R.id.title);
		etPassword = (EditText) view.findViewById(R.id.password);
		showPassword = (ImageView) view.findViewById(R.id.show_password);
		show = false;
		TextView tvConfirm = (TextView) view.findViewById(R.id.connect);
		TextView tvCancel = (TextView) view.findViewById(R.id.cancel);

		tvTitle.setText(title);

		showPassword.setOnClickListener(new clickListener());
		tvConfirm.setOnClickListener(new clickListener());
		tvCancel.setOnClickListener(new clickListener());

		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高
		lp.width = (int) (d.widthPixels * 0.98); // 宽度设置为屏幕的0.94
		lp.height = (int) (d.heightPixels * 0.42); // 高度设置为屏幕的0.38
		dialogWindow.setAttributes(lp);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				showKeyboard();
			}
		},300);
	}
    
    @Override
    public void dismiss() {
        View view = getCurrentFocus();
        if(view instanceof TextView){
            hideKeyboard();
        }
 
        super.dismiss();
    }

	private void showKeyboard() {
		etPassword.setFocusable(true);
		etPassword.setFocusableInTouchMode(true);
		etPassword.requestFocus();
		InputMethodManager imm = (InputMethodManager) etPassword.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(etPassword,0);
	}
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) etPassword.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
		}
	}

	public void setClicklistener(ClickListenerInterface clickListenerInterface) {
		this.clickListenerInterface = clickListenerInterface;
	}

	private class clickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
				case R.id.show_password:
					show = !show;
					if (show) {
						etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
						showPassword.setImageResource(R.drawable.password_hide);
					} else {
						etPassword.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
						showPassword.setImageResource(R.drawable.password_show);
					}
					etPassword.setSelection(etPassword.getText().toString().length());//将光标移至文字末尾
					break;
				case R.id.connect:
					hideKeyboard();
					clickListenerInterface.doConfirm(title, etPassword.getText().toString());
					break;
				case R.id.cancel:
					clickListenerInterface.doCancel();
					break;
			}
		}

	}

}