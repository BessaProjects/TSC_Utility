package com.tsc.printutility.View.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tsc.printutility.R;
import com.tsc.printutility.View.MainActivity;

import butterknife.OnClick;

public class PrintFragment extends BaseFragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_printmode);
        return mView;
    }

    @OnClick({R.id.printmode_barcode, R.id.printmode_file, R.id.printmode_test})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.printmode_barcode:
                ((MainActivity)mContext).gotoFragment(MainActivity.FragmentPage.PAGE_PRINT_BARCODE);
                break;
            case R.id.printmode_file:
                ((MainActivity)mContext).gotoFragment(MainActivity.FragmentPage.PAGE_PRINT_FILE);
                break;
            case R.id.printmode_test:
                ((MainActivity)mContext).gotoFragment(MainActivity.FragmentPage.PAGE_PRINT_WEB);
                break;
        }
    }
}
