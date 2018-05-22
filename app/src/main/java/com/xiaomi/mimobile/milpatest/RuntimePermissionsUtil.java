package com.xiaomi.mimobile.milpatest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class RuntimePermissionsUtil {

    public static final int MIUI_RUNTIME_PERMISSION_REQUESTCODE = 0x000;

    public static final String RUNTIME_PERMISSION_SMARTCARD = "org.simalliance.openmobileapi.SMARTCARD";

    public static final String RUNTIME_PERMISSION_LIST_SMARTCARD[] =  {RUNTIME_PERMISSION_SMARTCARD};


    @TargetApi(23)
    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(23)
    public static void requestRuntimePermissions(Activity activity, String[] permissionList) {
        List<String> requestPermission = new ArrayList<String>();
        for (String permission : permissionList) {
            if (!isPermissionGranted(activity, permission)) {
                requestPermission.add(permission);
            }
        }

        if (requestPermission.size() > 0) {
            Log.i("RuntimePermissionsUtil", "requestPermissions");
            ActivityCompat.requestPermissions(activity,
                    requestPermission.toArray(new String[requestPermission.size()]),
                    MIUI_RUNTIME_PERMISSION_REQUESTCODE);
        }
    }

}
