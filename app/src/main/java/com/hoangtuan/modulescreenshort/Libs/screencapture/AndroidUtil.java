package com.hoangtuan.modulescreenshort.Libs.screencapture;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;


public class AndroidUtil {


    public static boolean checkSelfPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }
}
