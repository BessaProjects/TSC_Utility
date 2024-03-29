package com.tsc.printutility.Controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Looper;

import com.example.tscdll.TSCActivity;
import com.example.tscdll.TscWifiActivity;
import com.tsc.printutility.Constant;
import com.tsc.printutility.Entity.DeviceInfo;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.Sqlite.MediaInfoController;
import com.tsc.printutility.Util.ImgUtil;
import com.tsc.printutility.Util.PrefUtil;
import com.tsc.printutility.View.BaseActivity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class PrinterController {

    private static final String TAG = PrinterController.class.getSimpleName();

    public static final String COMMAND_HEIGHT = "OUT GETSETTING$(\"CONFIG\", \"TSPL\",\"PAPER SIZE\")";
    public static final String COMMAND_WIDTH = "OUT GETSETTING$(\"CONFIG\", \"TSPL\",\"PAPER WIDTH\")";
    public static final String COMMAND_DEVICE_NAME = "OUT GETSETTING$(\"SYSTEM\", \"INFORMATION\",\"MODEL\")";
    public static final String COMMAND_BATTERY = "OUT GETSENSOR(\"BATTERY CAP\")";
    public static final String COMMAND_SPEED = "OUT GETSETTING$(\"CONFIG\", \"TSPL\",\"SPEED\")";
    public static final String COMMAND_DENSITY = "OUT GETSETTING$(\"CONFIG\", \"TSPL\",\"DENSITY\")";
    public static final String COMMAND_SENSOR = "OUT GETSETTING$(\"CONFIG\", \"SENSOR\",\"SENSOR TYPE\")";
    public static final String COMMAND_DPI = "OUT GETSETTING$(\"SYSTEM\", \"INFORMATION\",\"DPI\")";

    private static final String[] COMMAND_LIST = new String[]{COMMAND_HEIGHT, COMMAND_WIDTH, COMMAND_DPI, COMMAND_DEVICE_NAME, COMMAND_BATTERY, COMMAND_SPEED, COMMAND_DENSITY, COMMAND_SENSOR};

    private Queue<String> mCommandQueue = new PriorityQueue<>();

    private static PrinterController sInstance;
    private Context mContext;

    private TscWifiActivity mWifi;
    private TSCActivity mBle;

    private Thread mConnectThread;
    private Thread mPrintThread;

    private DeviceInfo mInfo = new DeviceInfo();

    private HashMap<String, OnConnectListener> mConnectList = new HashMap<>();

    private boolean mIsCommandSending = false;

    private static final int DELAY_TIMER = 300;

    public interface OnPrintCompletedListener{
        void onCompleted(boolean isSuccess, String message);
    }

    public interface OnConnectListener{
        void onConnect(boolean isSuccess);
    }

    public static PrinterController getInstance(Context context){
        if(sInstance == null)
            sInstance = new PrinterController();
        sInstance.mContext = context;
        return sInstance;
    }

    public DeviceInfo getDeviceInfo(){
        return mInfo;
    }

    public void clearDeviceInfo(){
        mInfo = new DeviceInfo();
    }

    public void addOnConnectListener(String name, OnConnectListener listener){
        mConnectList.put(name, listener);
    }

    private MediaInfo getMediaInfo(){
        MediaInfoController controller = new MediaInfoController(mContext);
        long currentMediaId = PrefUtil.getLongPreference(mContext, Constant.Pref.PARAM_MEDIA_ID, -1);
        if(currentMediaId != -1) {
            return controller.get(currentMediaId);
        }
        else{
            MediaInfo info = new MediaInfo();
            info.setUnit(Constant.ParamDefault.UNIT);
            info.setWidth(Constant.ParamDefault.WIDTH);
            info.setHeight(Constant.ParamDefault.HEIGHT);
            return info;
        }
    }

//    private String getSetupCommand(int width, int height, int speed, int density, int sensor){
//        String size = "SIZE " + width + " mm" + ", " + height + " mm";
//        String speed_value = "SPEED " + speed;
//        String density_value = "DENSITY " + density;
//        String sensor_value = "";
//        if (sensor == 0) {
//            sensor_value = "GAP 2 mm, 0 mm";
//        } else if (sensor == 1) {
//            sensor_value = "BLINE 2 mm, 0 mm";
//        } else if (sensor == 2) {
//            sensor_value = "GAP 2 mm, 0 mm\r\nGAP 0 mm, 0 mm";
//        }
//
//        return size + "\r\n" + speed_value + "\r\n" + density_value + "\r\n" + sensor_value + "\r\n";
//    }

    public String getSetupSizeCommand(int width, int height, int sensor){
        String size = "SIZE " + width + " mm" + ", " + height + " mm";
        String sensor_value = "";
        if (sensor == 0) {
            sensor_value = "GAP 2 mm, 0 mm";
        } else if (sensor == 1) {
            sensor_value = "BLINE 2 mm, 0 mm";
        } else if (sensor == 2) {
            sensor_value = "GAP 2 mm, 0 mm\r\nGAP 0 mm, 0 mm";
        }
        return size + "\r\n" + sensor_value + "\r\n";
    }

    public String getSetupSpeedCommand(int speed){
        String speed_value = "SPEED " + speed;
        return speed_value + "\r\n";
    }

    public String getSetupDensityCommand(int density){
        String density_value = "DENSITY " + density;
        return density_value + "\r\n";
    }

    public void connectBlePrinter(final String ipaddress, final OnConnectListener onConnectListener){
        if(mConnectThread != null)
            mConnectThread.interrupt();

        ((BaseActivity)mContext).showProgress(null);
        mConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if(mBle == null)
                    mBle = new TSCActivity();
                final String resultBtOpen = mBle.openport(ipaddress);
                System.out.println(TAG + " connectBlePrinter result:" + resultBtOpen);

                ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(resultBtOpen.equals("1")) {
                            onConnectListener.onConnect(true);
                            requestDeviceInfo();
                            return;
                        }
                        ((BaseActivity)mContext).dismissProgress();
                        onConnectListener.onConnect(false);
                    }
                });
            }
        });
        mConnectThread.start();
    }

    public boolean isBlePrinterConnected(){
        if(mBle != null){
            String result = mBle.status(DELAY_TIMER);
            if(result.equals("-1"))
                return false;
            else
                return true;
        }
        return false;
    }

    private String setupBleParam(String command){
        String sp = "-2";
        if(mBle != null) {
            sp = mBle.sendcommand(command, DELAY_TIMER);
        }
        System.out.println(TAG + " ble setup result:" + sp);
        return sp;
    }

    public void setup(final String command, final OnPrintCompletedListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if(setupBleParam(command).equals("1")) {
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted(true, null);
                        }
                    });
                }
                else if(setupWifiParam(command).equals("1")){
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted(true, null);
                        }
                    });
                }
                else{
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted(false, null);
                        }
                    });
                }
            }
        }).start();
    }

    public void print(final Object source, final String ipaddress, final OnPrintCompletedListener listener){
        if(mPrintThread != null)
            mPrintThread.interrupt();
        
        mPrintThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String resultBtOpen = "-2";
                if(!isBlePrinterConnected()) {
                    mBle = new TSCActivity();
                    resultBtOpen = mBle.openport(ipaddress);
                }
                else{
                    resultBtOpen = "1";
                }
                System.out.println(TAG + " ble openport result:" + resultBtOpen);
                if(resultBtOpen.equals("1")) {
                    MediaInfo info = getMediaInfo();
                    boolean isResize = PrefUtil.getBooleanPreference(mContext, Constant.Pref.PARAM_IS_RESIZE, Constant.ParamDefault.IS_RESIZE);
                    float dpi = Constant.ParamDefault.getRealDpi(PrefUtil.getIntegerPreference(mContext, Constant.Pref.PARAM_DPI, Constant.ParamDefault.DPI));

                    int count = 0;
                    if(source instanceof PdfRenderer) {
                        count = ((PdfRenderer)source).getPageCount();
                    }
                    else if(source instanceof List<?>){
                        count = ((List<Bitmap>)source).size();
                    }
                    else if(source instanceof Bitmap)
                        count = 1;

                    if(count != 0){
                        for(int i = 0; i < count; i++) {
                            mBle.clearbuffer();
                            Bitmap b = null;
                            if(source instanceof PdfRenderer) {
                                b = ImgUtil.getBitmapFromPdf(mContext, (PdfRenderer)source, i, PdfRenderer.Page.RENDER_MODE_FOR_PRINT, false);
                            }
                            else if(source instanceof List<?>){
                                b = ((List<Bitmap>)source).get(i);
                            }
                            else if(source instanceof Bitmap)
                                b = (Bitmap) source;
                            if(b != null) {
                                String res = sendBimap(mBle, b, info, dpi, isResize);
                                System.out.println(TAG + " ble sendBitmap at index " + i + ", result:" + res);
                                b.recycle();
                                if(res.equals("-2")){
                                    listener.onCompleted(false, "Out Of Memory error");
                                }
                            }
                        }
                    }
                }
                else{
                    listener.onCompleted(false, "Bluetooth open port failed!");
                }
            }
        });
        mPrintThread.start();
    }

    public void connectWifiPrinter(final String ipaddress, final OnConnectListener onConnectListener){
        if(mConnectThread != null)
            mConnectThread.interrupt();

        ((BaseActivity)mContext).showProgress(null);
        mConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if(mWifi == null)
                    mWifi = new TscWifiActivity();
                final String resultBtOpen = mWifi.openport(ipaddress, 9100);

                ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(resultBtOpen.equals("1")) {
                            onConnectListener.onConnect(true);
                            requestDeviceInfo();
                            return;
                        }
                        ((BaseActivity)mContext).dismissProgress();
                        onConnectListener.onConnect(false);
                    }
                });
            }
        });
        mConnectThread.start();
    }

    public boolean isWifiPrinterConnected(){
        if(mWifi != null){
            String result = mWifi.status(DELAY_TIMER);
            if(result.equals("-1"))
                return false;
            else
                return true;
        }
        return false;
    }

    public void isWifiPrinterConnectedThread(final OnConnectListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mWifi != null) {
                    final String result = mWifi.status(DELAY_TIMER);
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result.equals("-1"))
                                listener.onConnect(false);
                            else
                                listener.onConnect(true);
                        }
                    });

                }
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConnect(false);
                    }
                });
            }
        }).start();
    }

    public String setupWifiParam(String command){
        String sp = "-2";
        if(mWifi != null) {
            sp = mWifi.sendcommand(command);
        }

        System.out.println(TAG + "wifi ble setup result:" + sp);
        return sp;
    }

    public void printByWifi(final Object source, final String ipaddress, final OnPrintCompletedListener listener){
        if(mPrintThread != null)
            mPrintThread.interrupt();

        mPrintThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String p = "-2";
                if(!isWifiPrinterConnected()){
                    mWifi = new TscWifiActivity();
                    p = mWifi.openport(ipaddress, 9100);
                }
                else{
                    p = "1";
                }
                System.out.println(TAG + " wifi openport result:" + p);
                if(p.equals("1")) {
                    MediaInfo info = getMediaInfo();
                    boolean isResize = PrefUtil.getBooleanPreference(mContext, Constant.Pref.PARAM_IS_RESIZE, Constant.ParamDefault.IS_RESIZE);
                    float dpi = Constant.ParamDefault.getRealDpi(PrefUtil.getIntegerPreference(mContext, Constant.Pref.PARAM_DPI, Constant.ParamDefault.DPI));

                    int count = 0;
                    if(source instanceof PdfRenderer) {
                        count = ((PdfRenderer)source).getPageCount();
                    }
                    else if(source instanceof List<?>){
                        count = ((List<Bitmap>)source).size();
                    }
                    else if(source instanceof Bitmap)
                        count = 1;
                    if(count != 0){
                        for(int i = 0; i < count; i++) {
                            mWifi.clearbuffer();
                            Bitmap b = null;
                            if(source instanceof PdfRenderer) {
                                b = ImgUtil.getBitmapFromPdf(mContext, (PdfRenderer)source, i, PdfRenderer.Page.RENDER_MODE_FOR_PRINT, false);
                            }
                            else if(source instanceof List<?>){
                                b = ((List<Bitmap>)source).get(i);
                            }
                            else if(source instanceof Bitmap)
                                b = (Bitmap) source;
                            if(b != null) {
                                String res = sendBimap(mWifi, b, info, dpi, isResize);
                                System.out.println(TAG + " wifi sendBitmap at index " + i + ", result:" + res);
                                b.recycle();
                                if(res.equals("-2")){
                                    listener.onCompleted(false, "Out Of Memory error");
                                }
                            }
                        }
                    }
                }
                else
                    listener.onCompleted(false, ipaddress + ":" + "9100 Open port failed!");
            }
        });
        mPrintThread.start();

    }

    private String sendBimap(Object tscSendCommand, Bitmap b, MediaInfo info, float dpi, boolean isResize){
        if(tscSendCommand == null)
            return "-2";
        try {
            if(tscSendCommand instanceof TscWifiActivity) {
                TscWifiActivity wf = (TscWifiActivity) tscSendCommand;
                if (isResize) {
                    if (info.getUnit() == MediaInfo.UNIT_IN) {
                        wf.sendbitmap(0, 0, Bitmap.createScaledBitmap(b, (int) (info.getWidth() * dpi), (int) (info.getHeight() * dpi), false));
                        System.out.println("sendBitmap UNIT_IN Resize:" + (info.getWidth() * dpi) + ", " + (info.getHeight() * dpi));
                    }
                    else {
                        wf.sendbitmap(0, 0, Bitmap.createScaledBitmap(b, (int) (info.getWidth() / 25.4f * dpi), (int) (info.getHeight() / 25.4 * dpi), false));
                        System.out.println("sendBitmap UNIT_MM Resize:" + (info.getWidth() / 25.4f * dpi) + ", " + (info.getHeight() / 25.4f * dpi));
                    }
                }
                else {
                    wf.sendbitmap(0, 0, b);
                    System.out.println("sendBitmap:" + b.getWidth() + ", " + b.getHeight());
                }
                return wf.printlabel(1, 1);
            }
            else{
                TSCActivity bt = (TSCActivity) tscSendCommand;
                if (isResize) {
                    if (info.getUnit() == MediaInfo.UNIT_IN) {
                        System.out.println("sendBitmap UNIT_IN Resize:" + (info.getWidth() * dpi) + ", " + (info.getHeight() * dpi));
                        bt.sendbitmap(0, 0, Bitmap.createScaledBitmap(b, (int) (info.getWidth() * dpi), (int) (info.getHeight() * dpi), false));
                    }
                    else {
                        System.out.println("sendBitmap UNIT_MM Resize:" + (info.getWidth() / 25.4f * dpi) + ", " + (info.getHeight() / 25.4f * dpi));
                        bt.sendbitmap(0, 0, Bitmap.createScaledBitmap(b, (int) (info.getWidth() / 25.4f * dpi), (int) (info.getHeight() / 25.4 * dpi), false));
                    }
                }
                else {
                    System.out.println("sendBitmap:" + b.getWidth() + ", " + b.getHeight());
                    bt.sendbitmap(0, 0, b);
                }
                return bt.printlabel(1, 1);
            }
        }
        catch (OutOfMemoryError error){
            return "-2";
        }
    }

    public void sendCommand(final String command, final int delay, final OnPrintCompletedListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("requestDeviceInfo ble:" + mBle);
                if(mWifi != null){
                    final String receivedData = mWifi.sendcommand_getstring(command, delay);
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted(true, receivedData.replace("\n", ""));
                        }
                    });
                    return;
                }
                else if(mBle != null){
                    final String receivedData = mBle.sendcommand_getstring(command, delay);
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCompleted(true, receivedData.replace("\n", ""));
                        }
                    });
                    return;
                }
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onCompleted(true, "Send command failed");
                    }
                });

            }
        }).start();
    }

    public void sendCommand(final String command, final OnPrintCompletedListener listener){
        sendCommand(command, 500, listener);
    }

    public void closeport(){
        closeport(null);
    }

    public void closeport(final OnPrintCompletedListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mBle != null) {
                    System.out.println("Ble disconnect result:" + mBle.closeport(200));
                    mBle = null;
                }

                if(mWifi != null) {
                    System.out.println("Wifi disconnect result:" + mWifi.closeport(200));
                    mWifi = null;
                }
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(listener != null)
                            listener.onCompleted(true, "");
                    }
                });

            }
        }).start();
    }

    /**
     * @param ipaddress
     * @param source
     * @param type
     * @param listener
     */
    public void printBarcode(final String ipaddress, final String source, final String type, final OnPrintCompletedListener listener){
        if(mPrintThread != null)
            mPrintThread.interrupt();

        mPrintThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String resultBtOpen = "-2";

                MediaInfo info = getMediaInfo();

                if(ipaddress.startsWith("192.168")){
                    if(!isWifiPrinterConnected()){
                        mWifi = new TscWifiActivity();
                        resultBtOpen = mWifi.openport(ipaddress, 9100);
                    }
                    else{
                        resultBtOpen = "1";
                    }
                    System.out.println(TAG + " wifi openport result:" + resultBtOpen);
                }
                else{
                    if(!isBlePrinterConnected()) {
                        mBle = new TSCActivity();
                        resultBtOpen = mBle.openport(ipaddress);
                    }
                    else{
                        resultBtOpen = "1";
                    }
                    System.out.println(TAG + " ble openport result:" + resultBtOpen);
                }

                if(resultBtOpen.equals("1")) {
                    String printResilt;
                    if(ipaddress.startsWith("192.168")){
                        mWifi.sendcommand("CLS\r\n");
                        printResilt = mWifi.barcode(10, 10, type, 100, 1,0, 1, 1, source);
                        mWifi.sendcommand("PRINT 1\r\n");
                    }
                    else{
                        mBle.sendcommand("CLS\r\n");
                        printResilt = mBle.barcode(10, 10, type, 100, 1,0, 1, 1, source);
                        mBle.sendcommand("PRINT 1\r\n");
                    }

                    System.out.println(TAG + " printResilt:" + printResilt);
                    if(printResilt.equals("1"))
                        listener.onCompleted(true, null);
                    else
                        listener.onCompleted(false, "Print barcode failed.");

                }
                else{
                    listener.onCompleted(false, "Bluetooth open port failed!");
                }
            }
        });
        mPrintThread.start();
    }

    private void requestDeviceInfo(){
        mCommandQueue.addAll(new ArrayList<>(Arrays.asList(COMMAND_LIST)));
        commandRequest();
    }

    public void addCommandQueue(String command){
        mCommandQueue.add(command);
        if(!mIsCommandSending)
            commandRequest();
    }

    private void commandRequest(){
        final String command = mCommandQueue.peek();
//        System.out.println(TAG + " commandRequest:" + command);
        if(command != null){
            mCommandQueue.poll();
            mIsCommandSending = true;
            sendCommand(command, DELAY_TIMER, new OnPrintCompletedListener() {
                @Override
                public void onCompleted(boolean isSuccess, String message) {
                    mIsCommandSending = false;
                    if(isSuccess) {
                        String result = message.replace("\n", "");
                        try {
                            System.out.println(TAG + " commandRequest:" + command + ", " + new String(result.getBytes(),"UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        try {
                            switch (command) {
                                case COMMAND_HEIGHT:
                                    mInfo.setHeight(result);
                                    break;
                                case COMMAND_WIDTH:
                                    mInfo.setWidth(result);
                                    break;
                                case COMMAND_DEVICE_NAME:
                                    mInfo.setName(result);
                                    break;
                                case COMMAND_BATTERY:
                                    mInfo.setBattery(result);
                                    break;
                                case COMMAND_SPEED:
                                    mInfo.setSpeed(result);
                                    break;
                                case COMMAND_DENSITY:
                                    mInfo.setDensity(result);
                                    break;
                                case COMMAND_SENSOR:
                                    mInfo.setSensor(result);
                                    break;
                                case COMMAND_DPI:
                                    mInfo.setDpi(result);
                                    break;
                            }
                        }
                        catch (Exception e){

                        }
                    }
                    commandRequest();
                }
            });
        }
        else{
            ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((BaseActivity)mContext).dismissProgress();
                    mIsCommandSending = false;
                    for (Map.Entry entry : mConnectList.entrySet()) {
                        ((OnConnectListener)entry.getValue()).onConnect(true);
                    }
                }
            });
        }
    }
}
