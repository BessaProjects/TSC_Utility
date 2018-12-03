package com.tsc.printutility.View;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.tsc.printutility.Constant;
import com.tsc.printutility.R;
import com.tsc.printutility.View.fragment.BaseFragment;
import com.tsc.printutility.View.fragment.CommandFragment;
import com.tsc.printutility.View.fragment.ConnectFragment;
import com.tsc.printutility.View.fragment.PrintBarcodeFragment;
import com.tsc.printutility.View.fragment.PrintFileFragment;
import com.tsc.printutility.View.fragment.PrintFragment;
import com.tsc.printutility.View.fragment.PrintWebFragment;
import com.tsc.printutility.View.fragment.SettingFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.main_tab_setting, R.drawable.crop_image_menu_flip, android.R.color.white);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.main_tab_command, R.drawable.crop_image_menu_flip, android.R.color.white);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.main_tab_connect, R.drawable.crop_image_menu_flip, android.R.color.white);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.main_tab_print, R.drawable.crop_image_menu_flip, android.R.color.white);

// Add items
        mBottomNavigation.addItem(item1);
        mBottomNavigation.addItem(item2);
        mBottomNavigation.addItem(item3);
        mBottomNavigation.addItem(item4);

        mBottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.color_main));

        mBottomNavigation.setAccentColor(Color.WHITE);
        mBottomNavigation.setInactiveColor(Color.parseColor("#B0B0B0"));

        mBottomNavigation.setForceTint(true);

        mBottomNavigation.setTranslucentNavigationEnabled(true);
        mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position){
                    case 0:
                        gotoFragment(FragmentPage.PAGE_SETTING);
                        break;
                    case 1:
                        gotoFragment(FragmentPage.PAGE_COMMAND);
                        break;
                    case 2:
                        gotoFragment(FragmentPage.PAGE_CONNECT);
                        break;
                    case 3:
                        gotoFragment(FragmentPage.PAGE_PRINT);
                        break;

                }
                return true;
            }
        });

        String webPrintFile = getIntent().getStringExtra(Constant.Extra.FILE_PATH);
        if(webPrintFile != null){
            gotoFragment(FragmentPage.PAGE_PRINT_WEB);
        }
        else{
            gotoFragment(FragmentPage.PAGE_SETTING);
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

}

