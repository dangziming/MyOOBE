package com.neostra.android.oobe.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

public class WifiUtils {
	// 上下文Context对象
	private Context mContext;
	// WifiManager对象
	private WifiManager mWifiManager;

    public enum WifiCipherType {
        WIFICIPHER_INVALID, WIFICIPHER_NOPASS, WIFICIPHER_WEP, WIFICIPHER_WPA
    }

	public WifiUtils(Context mContext) {
        this.mContext = mContext;
        initWifiManager();
    }

    private void initWifiManager() {
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * 判断手机是否连接Wifi
	 */
	public boolean wifiConnected() {
		// 获取ConnectivityManager对象
		ConnectivityManager conMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 获取NetworkInfo对象
		NetworkInfo info = conMgr.getActiveNetworkInfo();
		// 获取连接的方式为wifi
		State wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

		if (info != null && info.isAvailable() && wifi == State.CONNECTED) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 判断手机是否连接在特定的Wifi上
	 */
	public boolean isConnectedWifi(String ssid) {
        boolean isSuccess = false;
        boolean flag = false;
        if (wifiConnected()) {
            while (!flag && !isSuccess) {
                String currSSID = getCurrentWifiInfo().getSSID();
                if (currSSID != null)
                    currSSID = currSSID.replace("\"", "");
                int currIp = getCurrentWifiInfo().getIpAddress();
                if (currSSID != null && currSSID.equals(ssid) && currIp != 0) {
                    isSuccess = true;
                } else {
                    flag = true;
                }
            }
        }
        return isSuccess;
	}

	/**
	 * 获取当前手机所连接的wifi信息
	 */
	private WifiInfo getCurrentWifiInfo() {
		return mWifiManager.getConnectionInfo();
	}

	/**
	 * 搜索附近的热点信息
	 */
	public void scanWifi() {
        initWifiManager();
		// 开始扫描热点
		mWifiManager.startScan();
	}

	/**
	 * 返回所有热点数据
	 */
	public List<ScanResult> getScanWifiResult() {
        initWifiManager();
		return filterScanResult(mWifiManager.getScanResults());
	}

	/**
	 * 以 SSID 为关键字，过滤掉信号弱的选项
	 */
	private List<ScanResult> filterScanResult(final List<ScanResult> list) {
		LinkedHashMap<String, ScanResult> linkedMap = new LinkedHashMap<>(list.size());
		for (ScanResult rst : list) {
			if (linkedMap.containsKey(rst.SSID)) {
				if (rst.level > linkedMap.get(rst.SSID).level) {
					linkedMap.put(rst.SSID, rst);
				}
				continue;
			}
			linkedMap.put(rst.SSID, rst);
		}
		list.clear();
		list.addAll(linkedMap.values());
		return list;
	}

	/**
	 * 利用WifiConfiguration.KeyMgmt的管理机制，来判断当前wifi是否需要连接密码
	 */
	public boolean isHasPassword(String wifiSSID) {
		try {
			initWifiManager();
			// 得到当前连接的wifi热点的信息
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			// 得到当前WifiConfiguration列表，此列表包含所有已经连过的wifi的热点信息，未连过的热点不包含在此表中
			List<WifiConfiguration> wifiConfiguration = mWifiManager.getConfiguredNetworks();
			String currentSSID = wifiInfo.getSSID();
			if (currentSSID != null && currentSSID.length() > 2) {
				if (currentSSID.startsWith("\"") && currentSSID.endsWith("\"")) {
					currentSSID = currentSSID.substring(1, currentSSID.length() - 1);
				}
				if (wifiConfiguration != null && wifiConfiguration.size() > 0) {
					for (WifiConfiguration configuration : wifiConfiguration) {
						if (configuration != null && configuration.status == WifiConfiguration.Status.CURRENT) {
							String ssid = null;
							if (!TextUtils.isEmpty(configuration.SSID)) {
								ssid = configuration.SSID;
								if (configuration.SSID.startsWith("\"") && configuration.SSID.endsWith("\"")) {
									ssid = configuration.SSID.substring(1, configuration.SSID.length() - 1);
								}
							}
							if (TextUtils.isEmpty(currentSSID) || currentSSID.equalsIgnoreCase(ssid)) {
								//KeyMgmt.NONE表示无需密码
								return (!configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			//do nothing
		}
		//默认为需要连接密码
		return true;
	}

	/**
	 * 连接wifi 参数：wifi的ssid及wifi的密码
	 */
	public boolean connectWifi(final String ssid, final String pwd, final String passwordType) {
        initWifiManager();
		//mWifiManager.disconnect();
		return addNetwork(createWifiInfo(ssid, pwd, getWifiCipher(passwordType)));
	}

	/**
	 * 判断wifi热点支持的加密方式
	 */
	public static WifiCipherType getWifiCipher(String s) {

		if (s.isEmpty()) {
			return WifiCipherType.WIFICIPHER_INVALID;
		} else if (s.contains("WEP")) {
			return WifiCipherType.WIFICIPHER_WEP;
		} else if (s.contains("WPA") || s.contains("WPA2") || s.contains("WPS")) {
			return WifiCipherType.WIFICIPHER_WPA;
		} else {
			return WifiCipherType.WIFICIPHER_NOPASS;
		}
	}

	/**
	 * 创建WifiConfiguration对象 分为三种情况：1没有密码;2用wep加密;3用wpa加密
	 * 
	 * @param SSID
	 * @param password
	 * @param type
	 * @return
	 */
	private WifiConfiguration createWifiInfo(String SSID, String password, WifiCipherType type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.isExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

        if (type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }

        if (type == WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }

        if (type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

        }
		return config;
	}

    /**
     * 查看是否已经配置过这个网络
     */
	private WifiConfiguration isExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

    /**
     * 添加一个网络并连接 传入参数：WIFI发生配置类WifiConfiguration
     */
    private boolean addNetwork(WifiConfiguration config) {
        Log.i("WifiUtil- WifiWizardActivity:", "addNetwork()");
        WifiInfo wifiinfo = mWifiManager.getConnectionInfo();

        if (null != wifiinfo) {
            Log.i("WifiUtil- WifiWizardActivity:", "wifiinfo is not null");
            mWifiManager.disableNetwork(wifiinfo.getNetworkId());
        }

        boolean result = false;

        if (config.networkId > 0) {
            result = mWifiManager.enableNetwork(config.networkId, true);
            Log.i("WifiUtil- WifiWizardActivity:", "config.networkId > 0, result = " + result);
            mWifiManager.updateNetwork(config);
        } else {

            int i = mWifiManager.addNetwork(config);
            result = false;
            Log.i("WifiUtil- WifiWizardActivity:", "config.networkId <= 0, i = " + i);

            if (i >= 0) {

                mWifiManager.saveConfiguration();
                return mWifiManager.enableNetwork(i, true);
            }
        }

        return result;
    }

}
