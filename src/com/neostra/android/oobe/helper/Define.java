package com.neostra.android.oobe.helper;

public class Define {
    public static final String TAG = "neostraOOBEM";

    public static final int DATA_CONNECTION_BOTH  = 0;
    public static final int DATA_CONNECTION_WIFI  = 1;
    public static final int DATA_CONNECTION_LATER = 2;

    public static final int REQUEST_BACK          = 200;
    public static final int RESULT_BACK           = 2;

    @SuppressWarnings("rawtypes")
    public static String getTag(Class c) {
        return TAG + " - " + c.getSimpleName();
    }
}