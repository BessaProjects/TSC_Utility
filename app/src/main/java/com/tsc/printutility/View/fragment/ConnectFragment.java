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

import butterknife.BindView;
import butterknife.OnClick;

public class ConnectFragment extends BaseFragment{

    @BindView(R.id.connect_connect)
    View mConnect;
    @BindView(R.id.connect_disconnect)
    View mDisconnect;
    @BindView(R.id.connect_address)
    TextView mAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_connect);
        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();

        updateUI();
    }

    @OnClick({R.id.connect_wifi, R.id.connect_ble, R.id.connect_clear, R.id.connect_disconnect_action})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.connect_wifi:
                ((BaseActivity)mContext).showProgress("Discovering...");
                ((BaseActivity)mContext).showIpDeviceList(new BaseActivity.OnDeviceSelectedListener() {
                    @Override
                    public void onSelected(String name, String address) {
                        PrefUtil.setStringPreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE, Constant.DeviceType.WIFI);
                        PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_WIFI_ADDRESS, address);
                        if(PrinterController.getInstance(mContext).connectWifiPrinter(address)) {
                            ((BaseActivity)mContext).setConnect(true);
                            updateUI();
                            mAddress.setText(address);
                        }
                    }
                });
                break;
            case R.id.connect_ble:
                ((BaseActivity)mContext).showProgress("Discovering...");
                ((BaseActivity)mContext).showBleDeviceList(new BaseActivity.OnDeviceSelectedListener() {
                    @Override
                    public void onSelected(String name, String address) {
                        PrefUtil.setStringPreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE, Constant.DeviceType.BLUETOOTH);
                        PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_BT_ADDRESS, address);
                        PrefUtil.setStringPreference(mContext, Constant.Pref.DEVICE_BT_NAME, name);
                        if(PrinterController.getInstance(mContext).connectBlePrinter(address)) {
                            ((BaseActivity)mContext).setConnect(true);
                            updateUI();
                            mAddress.setText(address);
                        }
                    }
                });
                break;
            case R.id.connect_clear:
                PrefUtil.removePreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE);
                PrefUtil.removePreference(mContext, Constant.Pref.DEVICE_BT_ADDRESS);
                PrefUtil.removePreference(mContext, Constant.Pref.DEVICE_BT_NAME);
                PrefUtil.removePreference(mContext, Constant.Pref.DEVICE_WIFI_ADDRESS);
                Toast.makeText(mContext, "已清除連線紀錄", Toast.LENGTH_LONG).show();
                break;
            case R.id.connect_disconnect_action:
                PrinterController.getInstance(mContext).closeport(new PrinterController.OnPrintCompletedListener() {
                    @Override
                    public void onCompleted(boolean isSuccess, String message) {
                        if(isSuccess){
                            ((BaseActivity)mContext).setConnect(false);
                            updateUI();
                        }
                    }
                });
                break;
        }
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
