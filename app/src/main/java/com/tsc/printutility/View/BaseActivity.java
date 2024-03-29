package com.tsc.printutility.View;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.tsc.printutility.Constant;
import com.tsc.printutility.Controller.BleController;
import com.tsc.printutility.Controller.PrinterController;
import com.tsc.printutility.Controller.WifiContoller;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.Sqlite.MediaInfoController;
import com.tsc.printutility.Util.DeviceUtil;
import com.tsc.printutility.Util.PrefUtil;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import butterknife.ButterKnife;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import rebus.permissionutils.PermissionUtils;
import rebus.permissionutils.SimpleCallback;

public class BaseActivity extends AppCompatActivity {

    private KProgressHUD mProgress;
    private MaterialDialog mBleListDialog;

    protected MediaInfoController mMediaInfoController;
    public MediaInfo mDefaultMediaInfo;

    public boolean mIsConnected = false;
    public String mConnectIp = "";

    protected void onCreate(int layout) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(layout);
        ButterKnife.bind(this);
        mMediaInfoController = new MediaInfoController(this);
    }

    public MediaInfo getDefaultMediaInfo(){
        return mDefaultMediaInfo;
    }

    public interface OnDeviceSelectedListener {
        void onSelected(String name, String address);
    }

    public void showProgress(String label){
        if(this != null && !this.isFinishing() && !isProgressShowing()) {
            mProgress = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(label)
                    .setCancellable(true)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();
        }
    }

    public void dismissProgress(){
        if(mProgress != null && this != null && !this.isFinishing()) {
            mProgress.dismiss();
        }
    }

    public boolean isProgressShowing(){
        if(mProgress != null && mProgress.isShowing())
            return true;
        return false;
    }

    public void showBleDeviceList(final OnDeviceSelectedListener listener){

        System.out.println("showBleDeviceList:");
        BluetoothAdapter.getDefaultAdapter().enable();
        BleController.getInstance(this).getBleDeviceList(new BleController.OnBleDiscoveryFinishedListener() {
            @Override
            public void onFinished(final List<BluetoothDevice> list, boolean isFinished) {
                if(isFinished){
                    dismissProgress();

                    if(list.size() == 0){
                        Toast.makeText(BaseActivity.this, R.string.alert_device_cannot_find, Toast.LENGTH_LONG).show();
                    }
                }
                if(list.size() > 0) {
                    List<String> nameList = new ArrayList<>();
                    for (BluetoothDevice device : list) {
                        if (device.getName() != null && device.getName().length() > 0) {
                            nameList.add(device.getName());
                        } else
                            nameList.add(device.getAddress());
                    }
                    if (mBleListDialog != null && mBleListDialog.isShowing()) {
                        mBleListDialog.getBuilder().items(nameList);
                        mBleListDialog.notifyItemsChanged();
                    } else {
                        mBleListDialog = new MaterialDialog.Builder(BaseActivity.this)
                                .items(nameList)
                                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        return false;
                                    }
                                })
                                .dismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        if(BleController.getInstance(BaseActivity.this).isDiscovering()) {
                                            dismissProgress();
                                            BleController.getInstance(BaseActivity.this).cancelDiscovery();
                                        }
                                    }
                                })
                                .positiveText(R.string.general_connect).onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dismissProgress();
                                        dialog.dismiss();
                                        if(dialog.getSelectedIndex() < list.size() && dialog.getSelectedIndex() >= 0)
                                            listener.onSelected(list.get(dialog.getSelectedIndex()).getName(), list.get(dialog.getSelectedIndex()).getAddress());
                                    }
                                })
                                .negativeText(R.string.general_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
            }
        });
    }

    public void showIpDeviceList(final OnDeviceSelectedListener listener){
        WifiContoller.getInstance(this).discoveryPrinter(new WifiContoller.OnIpDiscoveryFinishedListener() {
            @Override
            public void onFinished(final List<String> list, boolean isFinished) {
                if(isFinished){
                    dismissProgress();

                    if(list.size() == 0){
                        Toast.makeText(BaseActivity.this, R.string.alert_device_cannot_find, Toast.LENGTH_LONG).show();
                    }
                }
                if(list.size() > 0) {
                    if (mBleListDialog != null && mBleListDialog.isShowing()) {
                        try {
                            mBleListDialog.getBuilder().items(list);
                            mBleListDialog.notifyItemsChanged();
                        }
                        catch (ConcurrentModificationException exception){
                        }
                    } else {
                        mBleListDialog = new MaterialDialog.Builder(BaseActivity.this)
                                .items(list)
                                .dismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        dismissProgress();
                                        WifiContoller.getInstance(BaseActivity.this).cancelDiscovery();
                                    }
                                })
                                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        return false;
                                    }
                                })
                                .dismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        if(WifiContoller.getInstance(BaseActivity.this).isDiscovering()) {
                                            dismissProgress();
                                            WifiContoller.getInstance(BaseActivity.this).cancelDiscovery();
                                        }
                                    }
                                })
                                .neutralText(R.string.general_refresh).onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        WifiContoller.getInstance(BaseActivity.this).cancelDiscovery();
                                        showIpDeviceList(listener);
                                    }
                                })
                                .positiveText(R.string.general_connect).onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dismissProgress();
                                        dialog.dismiss();
                                        if(dialog.getSelectedIndex() < list.size() && dialog.getSelectedIndex() >= 0)
                                            listener.onSelected(list.get(dialog.getSelectedIndex()), list.get(dialog.getSelectedIndex()));
                                    }
                                })
                                .negativeText(R.string.general_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
            }
        });
    }
    private void scanBleDevice(final Object source){
        showProgress(getString(R.string.device_discovering));
        showBleDeviceList(new OnDeviceSelectedListener() {
            @Override
            public void onSelected(final String name, final String address) {
                showProgress(getString(R.string.device_printing));
                printByBle(source, address, name);
            }
        });
    }

    private void printByBle(final Object source, final String address, final String name){
        PrinterController.getInstance(BaseActivity.this).print(source, address, new PrinterController.OnPrintCompletedListener() {
            @Override
            public void onCompleted(boolean isSuccess, String message) {
                dismissProgress();
                if (isSuccess) {
                    showDataTransferDialog(name + "\n" + address);
                }
                else
                    Toast.makeText(BaseActivity.this, R.string.general_something_wrong, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showDataTransferDialog(final String address){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialDialog.Builder(BaseActivity.this)
                        .cancelable(false)
                        .content(getString(R.string.device_confirm_completely_printed))
                        .positiveText(R.string.general_close).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
    }

    private void scanIpDevice(final Object source){
        showProgress(getString(R.string.device_discovering));
        showIpDeviceList(new OnDeviceSelectedListener() {
            @Override
            public void onSelected(String name, final String address) {
                showProgress(getString(R.string.device_printing));
                printByWifi(source, address);
            }
        });
    }

    private void printByWifi(Object source, final String address){
        PrinterController.getInstance(BaseActivity.this).printByWifi(source, address.replace("/", ""), new PrinterController.OnPrintCompletedListener() {
            @Override
            public void onCompleted(boolean isSuccess, String message) {
                dismissProgress();
                if (isSuccess) {
                    showDataTransferDialog(address);
                }
                else
                    Toast.makeText(BaseActivity.this, R.string.general_something_wrong, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showDevicePicker(final Object source){
        final String lastConnectedDevice = PrefUtil.getStringPreference(this, Constant.Pref.LAST_CONNECTED_DEVICE);
        final String address;
        String content;
        if(lastConnectedDevice != null && lastConnectedDevice.equals(Constant.DeviceType.WIFI)){
            address = PrefUtil.getStringPreference(this, Constant.Pref.DEVICE_WIFI_ADDRESS);
            content = getString(R.string.device_using_to_print, address);
        }
        else if(lastConnectedDevice != null && lastConnectedDevice.equals(Constant.DeviceType.BLUETOOTH)){
            address = PrefUtil.getStringPreference(this, Constant.Pref.DEVICE_BT_ADDRESS);
            content = getString(R.string.device_using_to_print, PrefUtil.getStringPreference(this, Constant.Pref.DEVICE_BT_NAME));
        }
        else{
            new MaterialDialog.Builder(BaseActivity.this)
                    .items(new String[]{Constant.DeviceType.BLUETOOTH, Constant.DeviceType.WIFI})
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            dialog.dismiss();

                            if(which == 0) {
                                if(!PermissionUtils.isGranted(BaseActivity.this, PermissionEnum.ACCESS_COARSE_LOCATION)) {
                                    PermissionManager.Builder()
                                            .permission(PermissionEnum.ACCESS_COARSE_LOCATION)
                                            .callback(new SimpleCallback() {
                                                @Override
                                                public void result(boolean allPermissionsGranted) {
                                                    if (allPermissionsGranted)
                                                        scanBleDevice(source);
                                                }
                                            })
                                            .ask(BaseActivity.this);
                                }
                                else
                                    scanBleDevice(source);
                            }
                            else {
                                scanIpDevice(source);
                            }

                            return true;
                        }
                    })
                    .show();
            return;
        }

        new MaterialDialog.Builder(BaseActivity.this)
                .content(content)
                .negativeText(R.string.general_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .positiveText(R.string.general_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if(lastConnectedDevice.equals(Constant.DeviceType.WIFI)){
                            printByWifi(source, address);
                        }
                        else if(lastConnectedDevice.equals(Constant.DeviceType.BLUETOOTH)){
                            printByBle(source, address, PrefUtil.getStringPreference(BaseActivity.this, Constant.Pref.DEVICE_BT_NAME));
                        }
                    }
                }).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handleResult(this, requestCode, permissions, grantResults);
    }

    public void setConnect(boolean connect){
        mIsConnected = connect;
    }

    public boolean isConnected(){
        return mIsConnected;
    }

    public String getConnectIp(){
        return mConnectIp;
    }

    @Override
    public void onDestroy(){
        dismissProgress();
        if(getClass().getSimpleName().equals(MainActivity.class.getSimpleName()))
            PrinterController.getInstance(this).closeport();
        super.onDestroy();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(!DeviceUtil.getTopActivityPackageName(this).equals(getPackageName())) {
            mIsConnected = false;
            PrinterController.getInstance(this).closeport();
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            for(int i = 0; i < fragmentManager.getFragments().size(); i++){
                fragmentManager.getFragments().get(i).onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
