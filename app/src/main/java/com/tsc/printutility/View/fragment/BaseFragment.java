package com.tsc.printutility.View.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
}
