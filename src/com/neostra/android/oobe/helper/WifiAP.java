package com.neostra.android.oobe.helper;

public class WifiAP implements Comparable<WifiAP> {
    private String ssid;
    private boolean connected;
    private boolean lock;

    public String getSSID() {
        return ssid;
    }

    public void setSSID(String wifiName) {
        this.ssid = wifiName;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean state) {
        this.connected = state;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean isLock) {
        this.lock = isLock;
    }

    // -------------------
    private String level;
    private String passwordType;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPasswordType() { return passwordType; }

    public void setPasswordType(String passwordType) {
        this.passwordType = passwordType;
    }

    @Override
    public int compareTo(WifiAP o) {
        int level1 = Integer.parseInt(this.getLevel());
        int level2 = Integer.parseInt(o.getLevel());
        return level2 - level1;
    }
}
