package com.neostra.android.oobe.helper;

import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class Utility {
    public static final String ROTATION_NAME = "default_rotation";
    public static final String BACKKEYLAUNCHED = "BackKeyLaunched";

    public static int getSetupComplete(Context context) {
        int setupComplete = 0;
        try {
            setupComplete = Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return setupComplete;
    }

    public static boolean isPackageExist(Context context, String packageName) {
        if (packageName == null || packageName.equals("")) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            Log.d(Define.TAG, "package does't exist:" + packageName);
            return false;
        }
    }

    public static boolean isIntentExist(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, 0);
        if (info == null) {
            Log.d(Define.TAG, "intent is invaid:" + intent);
            return false;
        } else {
            return true;
        }
    }

    public static void controlMobileConnection(Context context, boolean enabled) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (setMobileDataEnabledMethod != null) {
                setMobileDataEnabledMethod.invoke(tm, enabled);
            }
        } catch (Exception e) {
            Log.e(Define.TAG, "TelephonyManager setMobileDataEnabled error", e);
        }
    }

    public static void enableWiFiConnection(Context context) {
        try {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Log.e(Define.TAG, "Can't get WifiManager");
                return;
            }
            if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                return;
            }
            wifiManager.setWifiEnabled(true);
        } catch (Exception e) {
            Log.e(Define.TAG, "enableWiFiConnection exception:", e);
        }
    }

    public static void disableWiFiConnection(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifi.disconnect();
    }

    public static boolean has3GModule(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

         NetworkInfo localNetworkInfo = connectivityManager.getActiveNetworkInfo();
         if ((localNetworkInfo != null) && (localNetworkInfo.isConnected())) {
             return true;
         }
         return false;
    }

    public static boolean isRestricted(Context context, String restrictionKey) {
        final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        return um.hasUserRestriction(restrictionKey);
    }

    public static boolean hasGMS(Context context) {
        String gmsversion = SystemProperties.get("ro.com.google.gmsversion", "");
        return !TextUtils.isEmpty(gmsversion);
    }
    
    /**
     * 利用WifiConfiguration.KeyMgmt的管理机制，来判断当前wifi是否需要连接密码
     * @param wifiManager
     * @return true：需要密码连接，false：不需要密码连接
     */
    public static boolean checkIsCurrentWifiHasPassword(String currentWifiSSID,Context context,WifiManager mWifiManager) {
       try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // 得到当前连接的wifi热点的信息
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo(); 
            // 得到当前WifiConfiguration列表，此列表包含所有已经连过的wifi的热点信息，未连过的热点不包含在此表中
            List<WifiConfiguration> wifiConfiguration = wifiManager.getConfiguredNetworks();
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
    
}