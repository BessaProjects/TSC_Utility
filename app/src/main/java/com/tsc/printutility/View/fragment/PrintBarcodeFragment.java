package com.tsc.printutility.View.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.View.BaseActivity;
import com.tsc.printutility.View.MediaSettingActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class PrintBarcodeFragment extends BaseFragment {

    @BindView(R.id.print_barcode_paper_width)
    TextView mWidth;
    @BindView(R.id.print_barcode_paper_height)
    TextView mHeight;
    @BindView(R.id.print_barcode_sensor_type)
    TextView mSensorType;

    @BindView(R.id.print_barcode_value)
    EditText mValue;
    @BindView(R.id.print_barcode_type)
    EditText mType;
    @BindView(R.id.print_barcode_media_size)
    TextView mMediaSze;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_print_barcode);
        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();
        MediaInfo mediaInfo = ((BaseActivity)mContext).getDefaultMediaInfo();
        if(mediaInfo != null){
            mWidth.setText(mediaInfo.getWidth() + "");
            mHeight.setText(mediaInfo.getHeight() + "");
            mMediaSze.setText(mediaInfo.getName());
        }
    }

    @OnClick({R.id.print_barcode_action, R.id.print_barcode_media_size})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.print_barcode_action:

                break;
            case R.id.print_barcode_media_size:
                Intent i = new Intent(mContext, MediaSettingActivity.class);
                startActivity(i);
                break;
        }
    }
}
