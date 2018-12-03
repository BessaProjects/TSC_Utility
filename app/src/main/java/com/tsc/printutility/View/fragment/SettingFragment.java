package com.tsc.printutility.View.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tsc.printutility.Constant;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.PrefUtil;
import com.tsc.printutility.View.BaseActivity;
import com.tsc.printutility.View.MediaSettingActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingFragment extends BaseFragment {

    @BindView(R.id.setting_name)
    TextView mName;
    @BindView(R.id.setting_label_width)
    TextView mWidth;
    @BindView(R.id.setting_label_height)
    TextView mHeight;
    @BindView(R.id.setting_density)
    TextView mDensity;
    @BindView(R.id.setting_speed)
    TextView mSpeed;
    @BindView(R.id.setting_sensor_type)
    TextView mSensorType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_setting);

        return mView;
    }

    @Override
    public void onResume(){
        super.onResume();
        MediaInfo info = ((BaseActivity)mContext).getDefaultMediaInfo();
        if(info != null){
            if(info.getUnit() == MediaInfo.UNIT_IN) {
                mWidth.setText(info.getWidth() + "in");
                mHeight.setText(info.getHeight() + "in");
            }
            else{
                mWidth.setText(info.getWidth() + "mm");
                mHeight.setText(info.getHeight() + "mm");
            }

        }

        mDensity.setText(PrefUtil.getIntegerPreference(mContext, Constant.Pref.PARAM_DENSITY, Constant.ParamDefault.DENSITY) + "");
        mSpeed.setText(PrefUtil.getIntegerPreference(mContext, Constant.Pref.PARAM_SPEED, Constant.ParamDefault.SPEED) + "");
    }

    @OnClick({R.id.setting_speed_set, R.id.setting_media_size_set, R.id.setting_sensor_type_set, R.id.setting_density_set})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.setting_speed_set:
                showSeekbarDialog("Speed", 99, PrefUtil.getIntegerPreference(mContext, Constant.Pref.PARAM_SPEED, Constant.ParamDefault.SPEED), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PrefUtil.setIntegerPreference(mContext, Constant.Pref.PARAM_SPEED, (Integer) v.getTag());
                        mSpeed.setText(v.getTag() + "");
                    }
                });
                break;
            case R.id.setting_media_size_set:
                Intent i = new Intent(mContext, MediaSettingActivity.class);
                startActivity(i);
                break;
            case R.id.setting_sensor_type_set:
                break;
            case R.id.setting_density_set:
                showSeekbarDialog("Density", 15, PrefUtil.getIntegerPreference(mContext, Constant.Pref.PARAM_DENSITY, Constant.ParamDefault.DENSITY), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PrefUtil.setIntegerPreference(mContext, Constant.Pref.PARAM_DENSITY, (Integer) v.getTag());
                        mDensity.setText(v.getTag() + "");
                    }
                });
                break;
        }
    }

    private void showSeekbarDialog(String title, int max, int current, final View.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_seek_bar, null);
        final SeekBar seekBar = view.findViewById(R.id.dialog_seekbar);
        TextView maxTxt = view.findViewById(R.id.dialog_seekbar_max);
        final TextView currentTxt = view.findViewById(R.id.dialog_seekbar_current);

        TextView titleTxt = view.findViewById(R.id.dialog_seekbar_title);
        titleTxt.setText(title);
        currentTxt.setText(current + "");
        maxTxt.setText(max + "");
        seekBar.setMax(max);
        seekBar.setProgress(current);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTxt.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        view.findViewById(R.id.dialog_edit_btn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(seekBar.getProgress());
                dialog.dismiss();
                listener.onClick(v);
            }
        });

        view.findViewById(R.id.dialog_edit_btn_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
