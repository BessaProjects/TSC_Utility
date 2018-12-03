package com.tsc.printutility.View.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.tsc.printutility.R;

import butterknife.BindView;
import butterknife.OnClick;

public class CommandFragment extends BaseFragment{

    @BindView(R.id.command_send_data)
    TextView mSendData;
    @BindView(R.id.command_received_data)
    TextView mReceivedData;
    @BindView(R.id.command_input)
    EditText mInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_command);
        return mView;
    }

    @OnClick({R.id.command_send})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.command_send:
                mSendData.append(mInput.getText() + "\n");
                break;
        }
    }
}
