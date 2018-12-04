package com.tsc.printutility.Util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.List;

/**
 * Created by MarcusLiu on 2017/7/13.
 */

public class CommonUtil {

    private static final String TAG = CommonUtil.class.getSimpleName();

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");

            hexString.append(Integer.toHexString(intVal).toUpperCase() + ":");
        }
        if(hexString.length() > 0)
            return hexString.toString().substring(0, hexString.length() - 1);
        else
            return hexString.toString();
    }

    public static void gotoGooglePlay(Context context){
        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void openFacebookPage(Context context, String facebookPageID){
        String facebookUrl = "https://www.facebook.com/" + facebookPageID;
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
    }

    public static String getVersionCode(Context context){
        String appVersion = "";
        PackageManager manager = context.getPackageManager();
        try { PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            appVersion = info.versionName; //版本名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersion;
    }

    public static void gotoGoogleMap(Context context, String lat, String lon) {
        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps?saddr=MySupportMapFragment+Location&daddr=" + lat + "," + lon);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }
    }

    public static void measureView(View view){
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
    }

    public static String ignoreUnusedDigit(float number){
        String value = number + "";
        if(value.endsWith(".0")){
            return value.replace(".0", "");
        }
        return value;
    }

    public static boolean isAppOnForeground(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    public static float convertDpToPx(float dp, Context context) {
        float px = dp * getDensity(context);
        return px;
    }

    public static float convertPxToDp(float px, Context context) {
        float dp = px / getDensity(context);
        return dp;
    }

    public static float getDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }
}
