package com.tsc.printutility.Controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Looper;

import com.example.tscdll.TSCActivity;
import com.example.tscdll.TscWifiActivity;
import com.tsc.printutility.Constant;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.Sqlite.MediaInfoController;
import com.tsc.printutility.Util.ImgUtil;
import com.tsc.printutility.Util.PrefUtil;
import com.tsc.printutility.View.BaseActivity;

import java.util.List;

public class PrinterController {

    private static final String TAG = PrinterController.class.getSimpleName();

    private static PrinterController sInstance;
    private Context mContext;

    private TscWifiActivity mWifi;
    private TSCActivity mBle;

    private Thread mConnectThread;

    public interface OnPrintCompletedListener{
        void onCompleted(boolean isSuccess, String message);
    }

    public interface OnParamReceivedListener{
        void onReceived(Object message);
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

    public void connectBlePrinter(final String ipaddress, final OnConnectListener onConnectListener){
        if(mConnectThread != null)
            mConnectThread.interrupt();

        mConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if(mBle == null)
                    mBle = new TSCActivity();
                final String resultBtOpen = mBle.openport(ipaddress);
                System.out.println("connectBlePrinter result:" + resultBtOpen);

                ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(resultBtOpen.equals("1")) {
                            onConnectListener.onConnect(true);
                            return;
                        }
                        onConnectListener.onConnect(false);
                    }
                });
            }
        });
        mConnectThread.start();
    }

    public boolean isBlePrinterConnected(){
        if(mBle != null){
            String result = mBle.status();
            if(result.equals("-1"))
                return false;
            else
                return true;
        }
        return false;
    }

    private String setupBleParam(){
        MediaInfo info = getMediaInfo();

        String sp = "-2";
        if(mBle != null) {
            if (info.getUnit() == MediaInfo.UNIT_IN)
                sp = mBle.setup((int) (info.getWidth() * 25.4f), (int) (info.getHeight() * 25.4f), 4, 4, 0, 0, 0);
            else
                sp = mBle.setup((int) info.getWidth(), (int) info.getHeight(), 4, 4, 0, 0, 0);
        }
        System.out.println(TAG + " ble setup result:" + sp);
        return sp;
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

                    setupBleParam();
                    listener.onCompleted(true, null);

                    int count = 0;
                    if(source instanceof PdfRenderer) {
                        count = ((PdfRenderer)source).getPageCount();
                    }
                    else if(source instanceof List<?>){
                        count = ((List<Bitmap>)source).size();
                    }
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
                            return;
                        }
                        onConnectListener.onConnect(false);
                    }
                });
            }
        });
        mConnectThread.start();
    }

    public boolean isWifiPrinterConnected(){
        if(mWifi != null){
            String result = mWifi.status();
            if(result.equals("-1"))
                return false;
            else
                return true;
        }
        return false;
    }

    private String setupWifiParam(){
        MediaInfo info = getMediaInfo();

        String sp = "-2";
        if(mWifi != null) {
            if (info.getUnit() == MediaInfo.UNIT_IN)
                sp = mWifi.setup((int) (info.getWidth() * 25.4f), (int) (info.getHeight() * 25.4f), 4, 4, 0, 0, 0);
            else
                sp = mWifi.setup((int) info.getWidth(), (int) info.getHeight(), 4, 4, 0, 0, 0);
        }
        System.out.println(TAG + " Wifi setup result:" + sp);
        return sp;
    }


    Thread mPrintThread;
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

                    setupWifiParam();

                    listener.onCompleted(true, null);
                    int count = 0;
                    if(source instanceof PdfRenderer) {
                        count = ((PdfRenderer)source).getPageCount();
                    }
                    else if(source instanceof List<?>){
                        count = ((List<Bitmap>)source).size();
                    }
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

    public void sendCommand(final String command, final OnPrintCompletedListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String receivedData = "Send command failed !";
                if(mWifi != null){
                    boolean result = isWifiPrinterConnected();
                    if(result){
                        receivedData = mWifi.sendcommand_getstring(command, 1000);
                    }
                }
                else if(mBle != null){
                    boolean result = isBlePrinterConnected();
                    if(result){
                        receivedData = mBle.sendcommand_getstring(command, 1000);
                    }
                }
                listener.onCompleted(true, receivedData);
            }
        }).start();
    }

    public void closeport(){
        closeport(null);
    }

    public void closeport(final OnPrintCompletedListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mBle != null) {
                    System.out.println("Ble disconnect result:" + mBle.closeport(100));
                    mBle = null;
                }

                if(mWifi != null) {
                    System.out.println("Wifi disconnect result:" + mWifi.closeport(100));
                    mWifi = null;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
                    if(ipaddress.startsWith("192.168")){
                        setupWifiParam();
                        mWifi.barcode(0, 0, type, (int)info.getHeight(), 0,0, 0, 1, source);
                    }
                    else{
                        setupBleParam();
                        mWifi.barcode(0, 0, type, (int)info.getHeight(), 0,0, 0, 1, source);
                    }
                    listener.onCompleted(true, null);

                }
                else{
                    listener.onCompleted(false, "Bluetooth open port failed!");
                }
            }
        });
        mPrintThread.start();
    }


//    public String getDeviceName(OnParamReceivedListener listener){
//        if(mBle != null) {
//            System.out.println("Ble disconnect result:" + mBle.closeport(100));
//            mBle = null;
//
//        }
//
//        if(mWifi != null) {
//            System.out.println("Wifi disconnect result:" + mWifi.closeport(100));
//            mWifi = null;
//        }
//    }
}
