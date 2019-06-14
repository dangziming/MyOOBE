package com.neostra.android.oobe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.helper.UserHelper;
import com.neostra.android.oobe.helper.Utility;
import com.neostra.android.oobe.wizard.WizardManager;

import com.neostra.android.oobe.helper.PasswordDialog;
import com.neostra.android.oobe.helper.WifiAP;
import com.neostra.android.oobe.helper.WifiAdapter;
import com.neostra.android.oobe.helper.WifiUtils;
import com.neostra.android.oobe.helper.NetUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.setupwizardlib.util.SystemBarHelper;

public class WifiWizardActivity extends Activity implements OnItemClickListener {
    
    private static final String TAG = Define.getTag(WifiWizardActivity.class);
    
    private static final int WIFI_FAIL = 0;
	private static final int WIFI_OK = 10;
	private static final int SCAN_WIFI = 100;
	private static final int REFRESH_TIME = 10 * 1000; //刷新时间10秒
    
    private LinearLayout wifi_loading;
    private RelativeLayout connected_wifi;
    private TextView connected_ssid;
    private TextView connected_state;
    private ProgressBar connecting_icon;
    private ImageView connected_icon;
    private LinearLayout wifi_layout;
	private ListView wifi_lv;
	private Button next_button;
	private WifiUtils mUtils;
	private List<ScanResult> scanResult;

	private WifiAdapter adapter;
	private List<WifiAP> list = new ArrayList<>();
	private boolean firstScan;
	private boolean connecting;
	private String mSSID;
	private WifiBroadCastReceiver wifiBroadCastReceiver;

    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                gotoNext();
            } catch (ActivityNotFoundException a) {
                a.getMessage();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Dialog中键盘弹出时不将Activity顶起来
        Window win = getWindow();
        WindowManager.LayoutParams params = win.getAttributes();
        win.setSoftInputMode(params.SOFT_INPUT_ADJUST_NOTHING);
        
        setContentView(R.layout.activity_wifi_settings);
        Utility.enableWiFiConnection(this);
        
        mUtils = new WifiUtils(this);
		findViews();
		setLiteners();
		initList();
		initBroadcast();

        if (!UserHelper.isOwnerUser(this) && Utility.isNetworkConnected(this)) {
            Log.d(TAG, "Multi user: already has connection, skip");
            Intent i = WizardManager.getNextWizardIntent(getIntent(), RESULT_OK);
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        SystemBarHelper.hideSystemBars(getWindow());
    }

    public void goSkip(View v) {
        Log.d(TAG, "goSkip");

        gotoNext();
    }

    public void gotoNext() {
        Log.d(TAG, "gotoNext");

        Intent i = WizardManager.getNextWizardIntent(getIntent(), RESULT_OK);
        startActivity(i);
    }

    public void goBack(View v) {
        Log.d(TAG, "goBack");
        finish();
    }

	private void initBroadcast() {
		wifiBroadCastReceiver = new WifiBroadCastReceiver();
		IntentFilter filter = new IntentFilter();
		//filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi开关变化
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化
		this.registerReceiver(wifiBroadCastReceiver, filter);
	}

	private void findViews() {

        connected_wifi = (RelativeLayout) findViewById(R.id.connected_wifi);
        connected_ssid = (TextView) findViewById(R.id.connected_ssid);
        connected_state = (TextView) findViewById(R.id.connected_state);
        connecting_icon = (ProgressBar) findViewById(R.id.connecting_icon);
        connected_icon = (ImageView) findViewById(R.id.connected_icon);
        wifi_layout = (LinearLayout) findViewById(R.id.wifi_layout);

        wifi_loading = (LinearLayout) findViewById(R.id.wifi_loading);
        wifi_lv = (ListView) findViewById(R.id.wifi_lv);
        // wifi_lv.setDividerHeight(0);

		TextView sim_network = (TextView) findViewById(R.id.sim_network_value);
        String sim_network_name = NetUtils.getOperatorName(this);
        Log.i(TAG, "findViews, sim network name: " + sim_network_name);
        if(sim_network_name != null && !sim_network_name.equals("")) {
            sim_network.setText(sim_network_name);
        }
        
        next_button = (Button) findViewById(R.id.nextBtn);
        next_button.setEnabled(false);
        //next_button.setBackgroundColor(getResources().getColor(R.color.next_button_disable_color));

		adapter = new WifiAdapter(list);
		wifi_lv.setAdapter(adapter);
	}

	private void setLiteners() {
		wifi_lv.setOnItemClickListener(this);
	}

	public void initList() {
		firstScan = true;
		connecting = false;
		Log.i(TAG, "initList");

		//扫描附近WIFI信息
		mUtils.scanWifi();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(wifiBroadCastReceiver != null){
			this.unregisterReceiver(wifiBroadCastReceiver);
		}
	}

	public class WifiBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())){
				Log.i(TAG, "WifiBroadCastReceiver, SCAN_RESULTS");

				refreshWifiAPList(getWifiAPList());
				if (firstScan) {
					Log.i(TAG, "first scan success, progress dismiss");
                    wifi_loading.setVisibility(View.GONE);
                    wifi_lv.setVisibility(View.VISIBLE);
					firstScan = false;
				}

			}else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
				Log.i(TAG, "WifiBroadCastReceiver, NETWORK_STATE");

				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if(NetworkInfo.State.DISCONNECTED == info.getState()){
					Log.i(TAG, "DISCONNECTED: " + info.getDetailedState());
					if (NetworkInfo.DetailedState.FAILED == info.getDetailedState() ||
							NetworkInfo.DetailedState.DISCONNECTED == info.getDetailedState() ||
							NetworkInfo.DetailedState.IDLE == info.getDetailedState() ||
							NetworkInfo.DetailedState.BLOCKED == info.getDetailedState()) {
						next_button.setEnabled(false);
						if (connecting) {
							Log.i(TAG, "connecting fail, progress dismiss");
                            //Toast.makeText(WifiWizardActivity.this, getString(R.string.connect_fail), Toast.LENGTH_SHORT).show();
							showConnectFailed();
							connecting = false;
						}
					}
				}else if(NetworkInfo.State.CONNECTED == info.getState()){
					Log.i(TAG, "CONNECTED");
					refreshWifiAPList(getWifiAPList());
					next_button.setEnabled(true);
					if (connecting) {
						Log.i(TAG, "connecting success, progress dismiss");
						showConnected();
						connecting = false;
					}
                    mHandler.postDelayed(runnable, 1000);

				}

			}
		}
	}

	private void showConnecting(String ssid) {
        connected_wifi.setVisibility(View.VISIBLE);
		wifi_layout.setVisibility(View.VISIBLE);
        connected_ssid.setText(ssid);
        connected_state.setText(getString(R.string.connecting));
        connecting_icon.setVisibility(View.VISIBLE);
        connected_icon.setVisibility(View.GONE);
	}

    private void showConnected() {
        connected_state.setText(getString(R.string.connected));
        connecting_icon.setVisibility(View.GONE);
        connected_icon.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        // new Handler().postDelayed(new Runnable() {
        //     @Override
        //     public void run() {
        //         gotoNext();
        //     }
        // },
        // 1000);
    }

	private void showConnectFailed() {
        connected_state.setText(getString(R.string.connect_fail));
        connecting_icon.setVisibility(View.GONE);
        connected_icon.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
	}

    private boolean refreshing = false;
	private void refreshWifiAPList(List<ScanResult> scanResults) {
        if(!refreshing) {
            refreshing = true;
            list.clear();
            int index = -1;
            WifiAP wifiConnectedAP = null;
            if(scanResults != null && scanResults.size() != 0) {
                for(int i = 0;i < scanResults.size();i++) {
                    WifiAP wifiAP = new WifiAP();
                    String ap_ssid = scanResults.get(i).SSID;
                    wifiAP.setSSID(ap_ssid);
                    if (mUtils.isHasPassword(ap_ssid)) {
                        wifiAP.setLock(true);

                    } else {
                        wifiAP.setLock(false);
                    }
                    if (mUtils.isConnectedWifi(ap_ssid)) {
                        wifiAP.setConnected(true);
                        index = i;
                        wifiConnectedAP = wifiAP;
                    } else {
                        wifiAP.setConnected(false);
                    }
                    wifiAP.setLevel(scanResults.get(i).level+"");
                    wifiAP.setPasswordType(scanResults.get(i).capabilities);
                    list.add(wifiAP);
					Collections.sort(list);
                    adapter.notifyDataSetChanged();
                }
            }
            if(index != -1){
                // list.remove(index);
                // list.add(0, wifiConnectedAP);
                // adapter.notifyDataSetChanged();
                connected_wifi.setVisibility(View.VISIBLE);
                wifi_layout.setVisibility(View.VISIBLE);
                connected_ssid.setText(wifiConnectedAP.getSSID());
                showConnected();
            }
            refreshing = false;
        }
	}

	private List<ScanResult> getWifiAPList() {
        scanResult = mUtils.getScanWifiResult();
        return scanResult;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		WifiAP ap = list.get(position);
		mSSID = ap.getSSID();
		if (ap.isLock()) {
			inputWifiPassword(this, mSSID, ap.getPasswordType());
		} else {
            connecting = true;
            Log.i(TAG, "connect unlock wifi, show dialog");
            showConnecting(mSSID);
			dealWithConnect(mSSID, null, ap.getPasswordType());
		}
	}

    public void inputWifiPassword(final Context context, final String ssid, final String passwordType) {
        final PasswordDialog passwordDialog = new PasswordDialog(context, ssid);
        passwordDialog.setCanceledOnTouchOutside(false);
        passwordDialog.show();
        passwordDialog.setClicklistener(new PasswordDialog.ClickListenerInterface() {
            @Override
            public void doConfirm(String ssid, String pwd) {
				// 判断密码输入情况
				if (TextUtils.isEmpty(pwd)) {
					Toast.makeText(WifiWizardActivity.this, getString(R.string.input_password), Toast.LENGTH_SHORT).show();
					return;
				}
				if (pwd.length() < 8) {
					Toast.makeText(WifiWizardActivity.this, "密码不够8位", Toast.LENGTH_SHORT).show();
					return;
				}
				connecting = true;
				Log.i(TAG, "connect wifi, show dialog");
				showConnecting(ssid);
				passwordDialog.dismiss();
				// 在子线程中处理各种业务
				dealWithConnect(ssid, pwd, passwordType);
            }

            @Override
            public void doCancel() {
                passwordDialog.dismiss();
            }
        });
    }

	private void dealWithConnect(final String ssid, final String pwd, final String passwordType) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
                Log.i(TAG, "dealWithConnect, begin connect wifi");
				mUtils.connectWifi(ssid, pwd, passwordType);
			}
		}).start();
	}

}
