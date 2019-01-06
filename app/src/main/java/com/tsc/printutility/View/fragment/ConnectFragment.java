package com.tsc.printutility.View.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tsc.printutility.Constant;
import com.tsc.printutility.Controller.PrinterController;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.PrefUtil;
import com.tsc.printutility.View.BaseActivity;
import com.tsc.printutility.View.MainActivity;

import butterknife.BindView;
import butterknife.OnClick;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import rebus.permissionutils.PermissionUtils;
import rebus.permissionutils.SimpleCallback;

public class ConnectFragment extends BaseFragment{

    @BindView(R.id.connect_connect)
    View mConnect;
    @BindView(R.id.connect_disconnect)
    View mDisconnect;
    @BindView(R.id.connect_device)
    TextView mDevice;
    @BindView(R.id.connect_address)
    TextView mAddress;
    @BindView(R.id.connect_battery)
    TextView mBattery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_connect);

        String battery = PrinterController.getInstance(mContext).getDeviceInfo().getBattery();
        if(battery != null) {
            if(battery.startsWith("0"))
                mBattery.setText("");
            else
                mBattery.setText(battery + "%");
            mDevice.setText(PrinterController.getInstance(mContext).getDeviceInfo().getName());
        }
        PrinterController.getInstance(mContext).addOnConnectListener(getClass().getSimpleName(), new PrinterController.OnConnectListener() {
            @Override
            public void onConnect(boolean isSuccess) {
                String battery = PrinterController.getInstance(mContext).getDeviceInfo().getBattery();
                if(battery.startsWith("0"))
                    mBattery.setText("");
                else
                    mBattery.setText(battery + "%");
                mDevice.setText(PrinterController.getInstance(mContext).getDeviceInfo().getName());
            }
        });
        updateUI();
        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    @OnClick({R.id.connect_wifi, R.id.connect_ble, R.id.connect_clear, R.id.connect_disconnect_action, R.id.connect_find_me_on, R.id.connect_find_me_off})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.connect_wifi:
                ((BaseActivity)mContext).showProgress(mContext.getString(R.string.device_discovering));
                ((BaseActivity)mContext).showIpDeviceList(new BaseActivity.OnDeviceSelectedListener() {
                    @Override
                    public void onSelected(String name, final String address) {
                        PrefUtil.setStringPreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE, Constant.DeviceType.WIFI);
                        PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_WIFI_ADDRESS, address);

                        PrinterController.getInstance(mContext).connectWifiPrinter(address, new PrinterController.OnConnectListener() {
                            @Override
                            public void onConnect(boolean isSuccess) {
                                if(isSuccess) {
                                    ((BaseActivity) mContext).setConnect(true);
                                    ((MainActivity)mContext).setBlockTab(false);
                                    mAddress.setText(address);
                                }
                                updateUI();
                            }
                        });
                    }
                });
                break;
            case R.id.connect_ble:
                if(!PermissionUtils.isGranted(mContext, PermissionEnum.ACCESS_COARSE_LOCATION)) {
                    PermissionManager.Builder()
                            .permission(PermissionEnum.ACCESS_COARSE_LOCATION)
                            .callback(new SimpleCallback() {
                                @Override
                                public void result(boolean allPermissionsGranted) {
                                    if (allPermissionsGranted)
                                        connectBle();
                                }
                            })
                            .ask(this);
                }
                else
                    connectBle();
                break;
            case R.id.connect_clear:
                PrefUtil.removePreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE);
                PrefUtil.removePreference(mContext, Constant.Pref.DEVICE_BT_ADDRESS);
                PrefUtil.removePreference(mContext, Constant.Pref.DEVICE_BT_NAME);
                PrefUtil.removePreference(mContext, Constant.Pref.DEVICE_WIFI_ADDRESS);
                Toast.makeText(mContext, R.string.alert_record_removed, Toast.LENGTH_LONG).show();
                break;
            case R.id.connect_disconnect_action:
                PrinterController.getInstance(mContext).closeport(new PrinterController.OnPrintCompletedListener() {
                    @Override
                    public void onCompleted(boolean isSuccess, String message) {
                        if(isSuccess){
                            ((BaseActivity)mContext).setConnect(false);
                            ((MainActivity)mContext).setBlockTab(true);
                            PrinterController.getInstance(mContext).clearDeviceInfo();
                            mDevice.setText("");
                            mBattery.setText("");
                            updateUI();
                        }
                    }
                });
                break;
            case R.id.connect_find_me_on:
                setSingleCommand("SET FINDME ON\r\n");
                break;
            case R.id.connect_find_me_off:
                setSingleCommand("SET FINDME OFF\r\n");
                break;
        }
    }

    private void connectBle(){
        ((BaseActivity)mContext).showProgress(mContext.getString(R.string.device_discovering));
        ((BaseActivity)mContext).showBleDeviceList(new BaseActivity.OnDeviceSelectedListener() {
            @Override
            public void onSelected(String name, final String address) {
                System.out.println("BLE address:" + address);
                PrefUtil.setStringPreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE, Constant.DeviceType.BLUETOOTH);
                PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_BT_ADDRESS, address);
                PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_BT_NAME, name);
                PrinterController.getInstance(mContext).connectBlePrinter(address, new PrinterController.OnConnectListener() {
                    @Override
                    public void onConnect(boolean isSuccess) {
                        if(isSuccess){
                            ((BaseActivity)mContext).setConnect(true);
                            ((MainActivity)mContext).setBlockTab(false);
                            mAddress.setText(address);
                        }
                        updateUI();
                    }
                });
            }
        });
    }

    private void updateUI(){
        if(((BaseActivity)mContext).isConnected()){
            mConnect.setVisibility(View.GONE);
            mDisconnect.setVisibility(View.VISIBLE);
            mAddress.setText(((BaseActivity)mContext).getConnectIp());
        }
        else{
            mConnect.setVisibility(View.VISIBLE);
            mDisconnect.setVisibility(View.GONE);
        }
    }
}
