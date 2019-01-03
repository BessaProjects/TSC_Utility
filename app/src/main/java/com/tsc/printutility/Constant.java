package com.tsc.printutility;

import com.tsc.printutility.Entity.MediaInfo;

public class Constant {

    public static class ParamDefault{
        public final static int PORT = 9100;
        public final static int WIDTH = 80;
        public final static int HEIGHT = 100;
        public final static int UNIT = MediaInfo.UNIT_IN;

        public final static int SPEED = 4;
        public final static int DENSITY = 4;

        public final static int BMP_WIDTH = 460;
        public final static int DPI = 200;
        public final static String[] DPI_LIST = new String[]{"200dpi", "300dpi", "600dpi"};

        public final static String[] SENSOR_TYPE = new String[]{"GAP", "BLINE"};

        public final static boolean IS_RESIZE = true;

        //    200dpi  => 8
//            300dpi => 11.8
//            600dpi => 23.

        public static float getRealDpi(int dpi){
//            if(dpi == 600){
//                return 23.6f;
//            }
//            else if(dpi == 300){
//                return 11.8f;
//            }
//            else
//                return 8f;
            return dpi;
        }
    }

    public static class Extra{
        public final static String FILE_PATH = "FILE_PATH";
        public final static String CROP_INDEX = "CROP_INDEX";
        public final static String CROP_IMAGE = "CROP_IMAGE";
    }

    public static class Pref{
        public final static String LAST_CONNECTED_DEVICE = "LAST_CONNECTED_DEVICE";
        public final static String DEVICE_BT_NAME = "DEVICE_BT_NAME";
        public final static String DEVICE_BT_ADDRESS = "DEVICE_BT_ADDRESS";
        public final static String DEVICE_WIFI_ADDRESS = "DEVICE_WIFI_ADDRESS";
        public final static String DEVICE_WIFI_PORT = "DEVICE_WIFI_PORT";
        public final static String CONNECTED_DEVICE_NAME = "CONNECTED_DEVICE_NAME";

        public final static String PARAM_WIDTH = "PARAM_WIDTH";
        public final static String PARAM_HEIGHT = "PARAM_HEIGHT";
        public final static String PARAM_DPI = "PARAM_DPI";
        public final static String PARAM_SPEED = "PARAM_SPEED";
        public final static String PARAM_DENSITY = "PARAM_DENSITY";

        public final static String PARAM_BMP_WIDTH = "PARAM_BMP_WIDTH";

        public final static String PARAM_IS_RESIZE = "PARAM_IS_RESIZE";
        public final static String PARAM_MEDIA_ID = "PARAM_MEDIA_ID";
    }

    public static class DeviceType{
        public final static String BLUETOOTH = "Bluetooth";
        public final static String WIFI = "WiFi";
    }
}
