package com.tsc.printutility.View.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tsc.printutility.R;
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

    @OnClick({R.id.connect_wifi, R.id.connect_ble, R.id.connect_disconnect_action})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.connect_wifi:

                break;
            case R.id.connect_ble:

                break;
            case R.id.connect_disconnect_action:
                break;
        }
    }
}
