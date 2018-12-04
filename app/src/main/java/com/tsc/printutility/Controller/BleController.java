package com.tsc.printutility.Controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.tsc.printutility.View.BaseActivity;

import java.util.ArrayList;
import java.util.List;


public class BleController {

    private static BleController sInstance;
    private Context mContext;

    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private OnBleDiscoveryFinishedListener mOnBleDiscoveryFinishedListener;
    private BluetoothAdapter mAdapter;

    public interface OnBleDiscoveryFinishedListener{
        void onFinished(List<BluetoothDevice> list, boolean isFinished);
    }

    public static BleController getInstance(Context context){
        if(sInstance == null)
            sInstance = new BleController();
        sInstance.mContext = context;
        return sInstance;
    }

    public BleController(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void cancelDiscovery(){
        if(mContext != null && mContext instanceof BaseActivity)
            ((BaseActivity)mContext).dismissProgress();
        mOnBleDiscoveryFinishedListener = null;
        mAdapter.cancelDiscovery();
        try {
            mContext.unregisterReceiver(mReceiver);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getBleDeviceList(OnBleDiscoveryFinishedListener listener){
        mDeviceList.clear();
        mOnBleDiscoveryFinishedListener = listener;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);
        mAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("aaadevice:" + action);
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(mOnBleDiscoveryFinishedListener != null)
                    mOnBleDiscoveryFinishedListener.onFinished(mDeviceList, true);
                if(mContext != null && mContext instanceof BaseActivity)
                    ((BaseActivity)mContext).dismissProgress();
                //discovery finishes, dismis progress dialog
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!checkRepeat(device.getAddress())){
                    mDeviceList.add(device);
                    if (mOnBleDiscoveryFinishedListener != null)
                        mOnBleDiscoveryFinishedListener.onFinished(mDeviceList, false);
                    System.out.println("aaadevice:" + device.getAddress());
                }
            }
        }
    };

    private boolean checkRepeat(String address){
        for(BluetoothDevice device:mDeviceList){
            if(device.getAddress().equals(address)){
                return true;
            }
        }
        return false;
    }
}
