package com.tsc.printutility.Controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;

import com.tsc.printutility.Util.CommonUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiContoller {

    private static WifiContoller sInstance;

    private Context mContext;

    private List<String> mIpList = new ArrayList<>();
    private boolean mIsDiscovering = false;

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

    public void cancelDiscovery(){
        System.out.println("cancelDiscovery:");
        mIpList.clear();
        if(mDiscoveryUDP != null){
            mDiscoveryUDP.interrupt();
            mDiscoveryUDP = null;
        }
        mIsDiscovering = false;
    }

    public boolean isDiscovering() {
        return mIsDiscovering;
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


    private Thread mDiscoveryUDP;

    public void discoveryPrinter(final OnIpDiscoveryFinishedListener listener) {

        if(mDiscoveryUDP != null){
            mDiscoveryUDP.interrupt();
            mDiscoveryUDP = null;
        }

        mIpList.clear();
        mIsDiscovering = true;
        mDiscoveryUDP = new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket ds = null;
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build());

                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .penaltyDeath()
                        .build());

                try {
                    byte[] udp_data = getUdpData();
                    DatagramPacket packet= new DatagramPacket(udp_data, udp_data.length);
                    packet.setAddress(InetAddress.getByName("255.255.255.255"));
                    packet.setPort(22368);

                    ds = new DatagramSocket(22368, InetAddress.getByName("0.0.0.0"));//muta test
                    ds.setBroadcast(true);
                    ds.send(packet);
                    ds.setSoTimeout(1500);

                    Thread.sleep(1500);

                    while(true){
                        ds.receive(packet);
                        String ip = getIpAddress(packet.getData());

                        byte[] bb = new byte[6];
                        for(int i = 22; i < 28; i++){
                            bb[i - 22] = packet.getData()[i];
                        }
                        System.out.println("GETIP: " + CommonUtil.byteArrayToHexString(bb));

                        if (!ip.equals("0.0.0.0") && !mIpList.contains(ip)) {
                            System.out.println("ShowIP:" + ip);
                            mIpList.add(ip);
                            Message m = new Message();
                            m.what = 1;
                            m.obj = listener;
                            mHandler.sendMessage(m);
                        }
                    }
                }
                catch (Exception e) {
                    if(ds != null)
                        ds.close();

                    mPingIpCount = 255;
                    Message m = new Message();
                    m.what = 1;
                    m.obj = listener;
                    mHandler.sendMessage(m);
                }
                mIsDiscovering = false;
            }
        });
        mDiscoveryUDP.start();
    }

    private String getIpAddress(byte[] udpbyte) {
        String ip = "";
        String singlebyte = "";
        int integer;

        for (int i = 0; i <= 3; i++){
            if(i == 1 || i == 2 || i == 3)
                ip = ip + ".";

            if(udpbyte[44 + i] < 0){
                integer = 256 + (int)udpbyte[44 + i];
                singlebyte = new String("" + integer);
            }
            else{
                integer = (int)udpbyte[44 + i];
                singlebyte = new String("" + integer);
            }

            ip = ip + singlebyte;
            if(ip == "0.0.0.0"){
                return ip;
            }
        }
        return ip;
    }


    private byte[] getUdpData(){
        byte[] udp_data = new byte[512];
        udp_data[0] = 0;
        udp_data[1] = 32;
        udp_data[2] = 0;
        udp_data[3] = 1;
        udp_data[4] = 0;
        udp_data[5] = 1;
        udp_data[6] = 8;
        udp_data[7] = 0;
        udp_data[8] = 0;
        udp_data[9] = 2;
        udp_data[10] = 0;
        udp_data[11] = 0;
        udp_data[12] = 0;
        udp_data[13] = 1;
        udp_data[14] = 0;
        udp_data[15] = 0;
        udp_data[16] = 1;
        udp_data[17] = 0;
        udp_data[18] = 0;
        udp_data[19] = 0;
        udp_data[20] = 0;
        udp_data[21] = 0;
        udp_data[22] = (byte) 255;
        udp_data[23] = (byte) 255;
        udp_data[24] = (byte) 255;
        udp_data[25] = (byte) 255;
        udp_data[26] = (byte) 255;
        udp_data[27] = (byte) 255;
        udp_data[28] = 0;
        udp_data[29] = 0;
        udp_data[30] = 0;
        udp_data[31] = 0;
        return udp_data;
    }
}
