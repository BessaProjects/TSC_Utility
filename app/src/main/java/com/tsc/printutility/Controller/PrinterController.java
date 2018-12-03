package com.tsc.printutility.Controller;

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

import java.util.List;

public class PrinterController {

    private static final String TAG = PrinterController.class.getSimpleName();

    private static PrinterController sInstance;
    private Context mContext;

    private TscWifiActivity mWifi;
    private TSCActivity mBle;

    public interface OnPrintCompletedListener{
        void onCompleted(boolean isSuccess, String message);
    }

    public static PrinterController getInstance(Context context){
        if(sInstance == null)
            sInstance = new PrinterController();
        sInstance.mContext = context;
        return sInstance;
    }


//    input label_x,label_y;
//
//switch(type){
//        case 'in':
//            setup(label_x*25.4,label_y*25.4)
//
//            break;
//        case 'mm':
//            setup(label_x,label_y)
//
//            break;
//    }

//    input label_x,label_y;
//    var dpi = 200;
//switch(type){
//        case 'in':
//            setup(label_x*25.4,label_y*25.4)
//            resize(label_x*dpi,label_y*dpi);
//            break;
//        case 'mm':
//            setup(label_x,label_y)
//            resize(label_x/25.4*200,label_y/25.4*200);
//            break;
//    }

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

    public boolean connectBlePrinter(String ipaddress){
        if(mBle == null)
            mBle = new TSCActivity();
        String resultBtOpen = mBle.openport(ipaddress);
        if(resultBtOpen.equals("1"))
            return true;
        return false;
    }

    public boolean isBlePrinterConnected(){
        String result = setupBleParam();
        if(result.equals("1"))
            return true;
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

    public void print(final Object source, final String ipaddress, final String name, final OnPrintCompletedListener listener){
        new Thread(new Runnable() {
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
                    PrefUtil.setStringPreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE, Constant.DeviceType.BLUETOOTH);
                    PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_BT_ADDRESS, ipaddress);
                    PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_BT_NAME, name);

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
                                b = ImgUtil.getBitmapFromPdf(mContext, (PdfRenderer)source, i, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
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
        }).start();
    }


    public boolean connectWifiPrinter(String ipaddress){
        if(mWifi == null)
            mWifi = new TscWifiActivity();
        String resultBtOpen = mWifi.openport(ipaddress, 9100);
        if(resultBtOpen.equals("1"))
            return true;
        return false;
    }


    public boolean isWifiPrinterConnected(){
        String result = setupWifiParam();
        if(result.equals("1"))
            return true;
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
                    PrefUtil.setStringPreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE, Constant.DeviceType.WIFI);
                    PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_WIFI_ADDRESS, ipaddress);

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
                                b = ImgUtil.getBitmapFromPdf(mContext, (PdfRenderer)source, i, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
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

//                    wf.closeport(delayTime + 2000);
//                    try {
//                        Thread.sleep(delayTime + 2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    listener.onCompleted(true, null);
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
                if(listener != null)
                    listener.onCompleted(true, "");
            }
        }).start();
    }
}
