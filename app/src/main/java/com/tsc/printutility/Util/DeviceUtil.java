package com.tsc.printutility.Util;

import android.content.Context;
import android.util.DisplayMetrics;

public class DeviceUtil {

    public static int[] getDisplay(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }
}
