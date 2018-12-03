package com.tsc.printutility.Controller;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class WifiContoller {

    private static WifiContoller sInstance;

    private Context mContext;

    private List<String> mIpList = new ArrayList<>();
    private List<Thread> mThreadPool = new ArrayList<>();

    public interface OnIpDiscoveryFinishedListener{
        void onFinished(List<String> list, boolean isFinish);
    }

    public static WifiContoller getInstance(Context context){
        if(sInstance == null)
            sInstance = new WifiContoller();
        sInstance.mContext = context;
        return sInstance;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    if(mPingIpCount == 255)
                        ((OnIpDiscoveryFinishedListener)msg.obj).onFinished(mIpList, true);
                    else
                        ((OnIpDiscoveryFinishedListener)msg.obj).onFinished(mIpList, false);
                    break;
            }
        }
    };

    private int mPingIpCount = 0;

    public void getIpList(final OnIpDiscoveryFinishedListener listener){
        cancelDiscovery();
        WifiManager wifii = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        final String ip = intToIp(wifii.getDhcpInfo().ipAddress, true);
        final String myIp = intToIp(wifii.getDhcpInfo().ipAddress, false);
        System.out.println("IP:" + myIp);
        mPingIpCount = 0;
        for (int i = 0; i < 256; i++) {
            final String host = ip + i;

            final int delay = i;
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        try{
                            Thread.sleep(15 * delay);
                        } catch(InterruptedException e){
                            e.printStackTrace();
                            return;
                        }

                        InetAddress inetAddress = InetAddress.getByName(host);
                        final String hostIp = inetAddress.toString().replace("/", "");

                        System.out.println("IP mPingIpCount:" + mPingIpCount);

                        boolean isReachable = inetAddress.isReachable(100);
                        mPingIpCount ++;
//                        if (NetworkUtil.isReachable(hostIp.toString(), Constant.ParamDefault.PORT) && !hostIp.toString().equals(myIp)){
                        if(isReachable && !hostIp.toString().equals(myIp)){
                            System.out.println("IP ok:" + hostIp + ", " + myIp);
                            mIpList.add(host);

                            Message m = new Message();
                            m.what = 1;
                            m.obj = listener;
                            mHandler.sendMessage(m);
                        }
                        else if(mPingIpCount == 255){
                            Message m = new Message();
                            m.what = 1;
                            m.obj = listener;
                            mHandler.sendMessage(m);
                        }

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            th.start();
            mThreadPool.add(th);
        }
    }

    public void cancelDiscovery(){
        System.out.println("cancelDiscovery:" + mThreadPool.size());
        for(Thread thread:mThreadPool){
            if(thread != null)
                thread.interrupt();
        }
        mThreadPool.clear();
        mIpList.clear();
    }

    public String intToIp(int i, boolean ignoreSubnet) {
        if(ignoreSubnet)
            return ( i & 0xFF)  +  "." +
                    ((i >> 8 ) & 0xFF) + "." +
                    ((i >> 16 ) & 0xFF) + ".";
        else
            return ( i & 0xFF)  +  "." +
                    ((i >> 8 ) & 0xFF) + "." +
                    ((i >> 16 ) & 0xFF) + "." +
                    ((i >> 24 ) & 0xFF) ;
    }
}
