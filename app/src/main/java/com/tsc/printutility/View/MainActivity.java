package com.tsc.printutility.View;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.tsc.printutility.Constant;
import com.tsc.printutility.Controller.PrinterController;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.CommonUtil;
import com.tsc.printutility.Util.PrefUtil;
import com.tsc.printutility.View.fragment.BaseFragment;
import com.tsc.printutility.View.fragment.CommandFragment;
import com.tsc.printutility.View.fragment.ConnectFragment;
import com.tsc.printutility.View.fragment.PrintBarcodeFragment;
import com.tsc.printutility.View.fragment.PrintFileFragment;
import com.tsc.printutility.View.fragment.PrintFragment;
import com.tsc.printutility.View.fragment.PrintWebFragment;
import com.tsc.printutility.View.fragment.SettingFragment;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import rebus.permissionutils.PermissionUtils;
import rebus.permissionutils.SimpleCallback;

public class MainActivity extends BaseActivity {

    public static final int REQUEST_MEDIA_SIZE_CHANGE = 989;

    public enum FragmentPage{
        PAGE_SETTING, PAGE_COMMAND, PAGE_CONNECT, PAGE_PRINT, PAGE_PRINT_BARCODE, PAGE_PRINT_WEB, PAGE_PRINT_FILE
    }

    private BaseFragment mCurrentFragment;

    public FragmentManager mFragmentManager;

    @BindView(R.id.bottom_navigation)
    AHBottomNavigation mBottomNavigation;
    @BindView(R.id.main_device_name)
    TextView mName;

    @BindView(R.id.toolbar_right_2)
    ImageView mBtnRight2;
    @BindView(R.id.toolbar_right)
    ImageView mBtnRight;

    private NfcAdapter mNfcAdapter;

    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.main_tab_setting, R.drawable.icon_setting, android.R.color.white);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.main_tab_command, R.drawable.icon_command, android.R.color.white);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.main_tab_connect, R.drawable.icon_connect, android.R.color.white);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.main_tab_print, R.drawable.icon_printer, android.R.color.white);

        mBottomNavigation.addItem(item1);
        mBottomNavigation.addItem(item4);
        mBottomNavigation.addItem(item2);
        mBottomNavigation.addItem(item3);

        mBottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.color_main));
        mBottomNavigation.setAccentColor(Color.YELLOW);
        mBottomNavigation.setInactiveColor(Color.WHITE);
        mBottomNavigation.setItemDisableColor(Color.WHITE);
        mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if(isConnected()) {
                    switch (position) {
                        case 0:
                            gotoFragment(FragmentPage.PAGE_SETTING);
                            break;
                        case 1:
                            gotoFragment(FragmentPage.PAGE_PRINT);
                            break;
                        case 2:
                            gotoFragment(FragmentPage.PAGE_COMMAND);
                            break;
                        case 3:
                            gotoFragment(FragmentPage.PAGE_CONNECT);
                            break;

                    }
                }
                return true;
            }
        });
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        onNewIntent(getIntent());

        String deviceName = PrinterController.getInstance(MainActivity.this).getDeviceInfo().getName();
        if(deviceName != null)
            mName.setText(deviceName);
        else {
            PrinterController.getInstance(this).addOnConnectListener(getClass().getSimpleName(), new PrinterController.OnConnectListener() {
                @Override
                public void onConnect(boolean isSuccess) {
                    if(isSuccess) {
                        setBlockTab(false);
                        mName.setText(PrinterController.getInstance(MainActivity.this).getDeviceInfo().getName());
                        if(mBottomNavigation.getCurrentItem() == 0)
                            gotoFragment(FragmentPage.PAGE_SETTING);
                    }
                }
            });
        }
    }

    public void setBlockTab(boolean isBlock){
        if(isBlock) {
            mName.setText("");
            mBottomNavigation.disableItemAtPosition(0);
            mBottomNavigation.disableItemAtPosition(1);
            mBottomNavigation.disableItemAtPosition(2);
        }
        else{
            mBottomNavigation.enableItemAtPosition(0);
            mBottomNavigation.enableItemAtPosition(1);
            mBottomNavigation.enableItemAtPosition(2);
        }
    }

    public void gotoFragment(FragmentPage page){
        hideKeyboard();
        switch (page){
            case PAGE_SETTING:
                mCurrentFragment = new SettingFragment();
                break;
            case PAGE_COMMAND:
                mCurrentFragment = new CommandFragment();
                break;
            case PAGE_CONNECT:
                mCurrentFragment = new ConnectFragment();
                break;
            case PAGE_PRINT:
                mCurrentFragment = new PrintFragment();
                break;
            case PAGE_PRINT_BARCODE:
                mCurrentFragment = new PrintBarcodeFragment();
                break;
            case PAGE_PRINT_WEB:
                mCurrentFragment = new PrintWebFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.Extra.FILE_PATH, getIntent().getStringExtra(Constant.Extra.FILE_PATH));
                mCurrentFragment.setArguments(bundle);
                break;
            case PAGE_PRINT_FILE:
                mCurrentFragment = new PrintFileFragment();
                break;
        }
        if(page == FragmentPage.PAGE_PRINT_WEB){
            mBtnRight.setVisibility(View.VISIBLE);
            mBtnRight2.setVisibility(View.VISIBLE);
        }
        else{
            mBtnRight.setVisibility(View.GONE);
            mBtnRight2.setVisibility(View.GONE);
        }
        changeContent();
    }

    public void changeContent() {
        if(!isFinishing()) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

            try {
                fragmentTransaction = fragmentTransaction.replace(R.id.main_framelayout, mCurrentFragment, mCurrentFragment.getClass().getSimpleName());
                fragmentTransaction.commitAllowingStateLoss();
            }
            catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
        Fresco.getImagePipeline().clearMemoryCaches();
    }

    @OnClick({R.id.toolbar_left, R.id.toolbar_right_2, R.id.toolbar_right})
    public void onClick(View v){
        Intent i;
        switch (v.getId()){
            case R.id.toolbar_left:
                if(mCurrentFragment != null){
                    String currentName = mCurrentFragment.getClass().getSimpleName();
                    if(currentName.equals(PrintBarcodeFragment.class.getSimpleName()) || currentName.equals(PrintWebFragment.class.getSimpleName())
                            || currentName.equals(PrintFileFragment.class.getSimpleName())){
                        mBottomNavigation.setCurrentItem(1);
                        gotoFragment(FragmentPage.PAGE_PRINT);
                    }
                }
                break;
            case R.id.toolbar_right:
                i = new Intent(this, MediaSettingActivity.class);
                startActivity(i);
                break;
            case R.id.toolbar_right_2:
                if(mCurrentFragment != null && mCurrentFragment instanceof PrintWebFragment) {
                    i = new Intent(this, CropperActivity.class);
                    i.putExtra(Constant.Extra.FILE_PATH, ((PrintWebFragment) mCurrentFragment).getFilePath());
                    startActivity(i);
                }
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        System.out.println("onNewIntent:" + intent + ", " + getIntent().getStringExtra(Constant.Extra.FILE_PATH));

        String action = intent.getAction();
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)){
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            MifareUltralight uTag = MifareUltralight.get(tagFromIntent);
            try {
                uTag.connect();
                byte[] data = uTag.readPages(2);
                byte[] macAddress = new byte[6];
                for(int i = 0; i < 6; i++){
                    macAddress[i] = data[i + 8];
                }
                uTag.close();

                final String address = CommonUtil.byteArrayToHexString(macAddress);
                System.out.println("Bluetooth mac address:" + address);
                if(!PermissionUtils.isGranted(this, PermissionEnum.ACCESS_COARSE_LOCATION)) {
                    PermissionManager.Builder()
                            .permission(PermissionEnum.ACCESS_COARSE_LOCATION)
                            .callback(new SimpleCallback() {
                                @Override
                                public void result(boolean allPermissionsGranted) {
                                    if (allPermissionsGranted)
                                        connectBle(address);
                                }
                            })
                            .ask(this);
                }
                else
                    connectBle(address);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String webPrintFile = getIntent().getStringExtra(Constant.Extra.FILE_PATH);
        if(webPrintFile != null){
            mBottomNavigation.setCurrentItem(1);
            gotoFragment(FragmentPage.PAGE_PRINT_WEB);
        }
    }

    private void connectBle(final String address){
        BluetoothAdapter.getDefaultAdapter().enable();
        PrinterController.getInstance(this).connectBlePrinter(address, new PrinterController.OnConnectListener() {
            @Override
            public void onConnect(boolean isSuccess) {
                if(isSuccess) {
                    PrefUtil.setStringPreference(MainActivity.this, Constant.Pref.LAST_CONNECTED_DEVICE, Constant.DeviceType.BLUETOOTH);
                    PrefUtil.setStringPreference(MainActivity.this, Constant.Pref.DEVICE_BT_ADDRESS, address);
                    PrefUtil.setStringPreference(MainActivity.this, Constant.Pref.DEVICE_BT_NAME, "");
                    Toast.makeText(MainActivity.this, address + " 已連線", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);

        if(!mIsConnected){
            long currentMediaId = PrefUtil.getLongPreference(this, Constant.Pref.PARAM_MEDIA_ID, -1);
            if(currentMediaId != -1 && mMediaInfoController != null) {
                mDefaultMediaInfo = mMediaInfoController.get(currentMediaId);
            }

            String lastConnectedDevice = PrefUtil.getStringPreference(this, Constant.Pref.LAST_CONNECTED_DEVICE);
            System.out.println(lastConnectedDevice);
            if(lastConnectedDevice != null){
                if(lastConnectedDevice.equals(Constant.DeviceType.WIFI)){
                    PrinterController.getInstance(this).isWifiPrinterConnectedThread(new PrinterController.OnConnectListener() {
                        @Override
                        public void onConnect(boolean isSuccess) {
                            if(!isSuccess) {
                                mConnectIp = PrefUtil.getStringPreference(MainActivity.this, Constant.Pref.DEVICE_WIFI_ADDRESS);
                                PrinterController.getInstance(MainActivity.this).connectWifiPrinter(mConnectIp, new PrinterController.OnConnectListener() {
                                    @Override
                                    public void onConnect(boolean isSuccess) {
                                        mIsConnected = isSuccess;
                                        if (mIsConnected) {
                                            setBlockTab(false);
                                            Toast.makeText(MainActivity.this, getString(R.string.alert_is_connected, mConnectIp), Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            setBlockTab(true);
                                            Toast.makeText(MainActivity.this, getString(R.string.alert_connection_failed, mConnectIp), Toast.LENGTH_LONG).show();
                                            mBottomNavigation.setCurrentItem(3);
                                            gotoFragment(FragmentPage.PAGE_CONNECT);
                                        }
                                    }
                                });
                            }
                            else{
                                setBlockTab(true);
                            }
                        }
                    });
                }
                else if(lastConnectedDevice.equals(Constant.DeviceType.BLUETOOTH)){
                    if(!PrinterController.getInstance(this).isBlePrinterConnected()){
                        mConnectIp = PrefUtil.getStringPreference(this, Constant.Pref.DEVICE_BT_ADDRESS);

                        BluetoothAdapter.getDefaultAdapter().enable();
                        PrinterController.getInstance(this).connectBlePrinter(mConnectIp, new PrinterController.OnConnectListener() {
                            @Override
                            public void onConnect(boolean isSuccess) {
                                mIsConnected = isSuccess;
                                if(mIsConnected) {
                                    setBlockTab(false);
                                    Toast.makeText(MainActivity.this, getString(R.string.alert_is_connected, mConnectIp), Toast.LENGTH_LONG).show();
                                }
                                else {
                                    setBlockTab(true);
                                    Toast.makeText(MainActivity.this, getString(R.string.alert_connection_failed, mConnectIp), Toast.LENGTH_LONG).show();
                                    mBottomNavigation.setCurrentItem(3);
                                    gotoFragment(FragmentPage.PAGE_CONNECT);
                                }
                            }
                        });
                    }
                    else
                        setBlockTab(true);
                }
            }
            else {
                mBottomNavigation.setCurrentItem(3);
                gotoFragment(FragmentPage.PAGE_CONNECT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }
}

