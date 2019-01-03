package com.tsc.printutility.Util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

public class DeviceUtil {

    public static int[] getDisplay(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    public static String getTopActivityPackageName(Context context){
        ActivityManager mActivityManager =(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        if(Build.VERSION.SDK_INT > 20){
            return mActivityManager.getRunningAppProcesses().get(0).processName;
        }
        else{
            return mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
    }
}
