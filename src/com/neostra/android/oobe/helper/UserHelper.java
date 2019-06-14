package com.neostra.android.oobe.helper;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

public class UserHelper {
    public static boolean isOwnerUser(Context context) {
        boolean bIsOwnerUser = true;
        try {
            if (UserHandle.MU_ENABLED && UserHandle.myUserId() != 0) {
                bIsOwnerUser = false;
            }
        } catch (Throwable  ex) {
            Log.d(Define.TAG, "NoClassDefFoundError!");
            bIsOwnerUser = true;
        }
        return bIsOwnerUser;
    }

    public static boolean isRestricted(Context context) {
        UserManager um = UserManager.get(context);
        return um.isLinkedUser();
    }

    public static boolean isGuest(Context context) {
        UserManager um = UserManager.get(context);
        return um.isGuestUser();
    }
}