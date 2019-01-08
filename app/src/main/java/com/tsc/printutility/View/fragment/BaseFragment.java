package com.tsc.printutility.View.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tsc.printutility.Controller.PrinterController;
import com.tsc.printutility.View.BaseActivity;

import butterknife.ButterKnife;

public class BaseFragment extends Fragment {


    protected Context mContext;
    protected View mView;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext = context;
    }

    public void onCreateView(LayoutInflater inflater, ViewGroup container, int layout) {
        mView = inflater.inflate(layout, container, false);
        ButterKnife.bind(this, mView);
    }

    public void setSingleCommand(String command){
        ((BaseActivity)mContext).showProgress(null);
        PrinterController.getInstance(mContext).sendCommand(command, 300, new PrinterController.OnPrintCompletedListener() {
            @Override
            public void onCompleted(boolean isSuccess, String message) {
                ((BaseActivity)mContext).dismissProgress();
            }
        });
    }
}
