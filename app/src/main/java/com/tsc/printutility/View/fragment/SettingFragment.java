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
import com.tsc.printutility.Controller.PrinterController;
import com.tsc.printutility.Entity.DeviceInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.View.BaseActivity;
import com.tsc.printutility.View.MainActivity;
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

    private DeviceInfo mInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_setting);

        mInfo = PrinterController.getInstance(mContext).getDeviceInfo();
        if(mInfo != null && mInfo.getName() != null && mInfo.getName().length() > 0) {
            updateDeviceInfo(mInfo);
        }

        PrinterController.getInstance(mContext).addOnConnectListener(getClass().getSimpleName(), new PrinterController.OnConnectListener() {
            @Override
            public void onConnect(boolean isSuccess) {
                updateDeviceInfo(PrinterController.getInstance(mContext).getDeviceInfo());

            }
        });
        return mView;
    }

    private void updateDeviceInfo(DeviceInfo info){
        mInfo = info;
        mName.setText(mInfo.getName());
        try {
            float dpi = Float.parseFloat(mInfo.getDpi());
            mWidth.setText(String.format("%.2f", (Float.parseFloat(mInfo.getWidth())/ dpi)) + " in");
            mHeight.setText(String.format("%.2f", (Float.parseFloat(mInfo.getHeight())/ dpi)) + " in");

        }catch (Exception e){

        }
        mDensity.setText(mInfo.getDensity() + "");
        mSpeed.setText(mInfo.getSpeed() + "");
        mSensorType.setText(mInfo.getSensor());
    }

    @OnClick({R.id.setting_speed_set, R.id.setting_media_size_set, R.id.setting_sensor_type_set, R.id.setting_density_set})
    public void onClick(View view){
        final PrinterController controller = PrinterController.getInstance(mContext);
        switch (view.getId()){
            case R.id.setting_speed_set:
                int speed = Constant.ParamDefault.SPEED;
                try {
                    speed = (int)Float.parseFloat(mInfo.getSpeed());
                }
                catch (Exception e){
                }
                showSeekbarDialog("Speed", 4, speed, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int speed = (Integer) v.getTag();
                        controller.setup(controller.getSetupSpeedCommand(speed), new PrinterController.OnPrintCompletedListener() {
                            @Override
                            public void onCompleted(boolean isSuccess, String message) {
                                if(isSuccess) {
                                    ((BaseActivity)mContext).showProgress(null);
                                    controller.addCommandQueue(PrinterController.COMMAND_SPEED);
                                }
                            }
                        });
                    }
                });
                break;
            case R.id.setting_media_size_set:
                Intent i = new Intent(mContext, MediaSettingActivity.class);
                startActivityForResult(i, MainActivity.REQUEST_MEDIA_SIZE_CHANGE);
                break;
//            case R.id.setting_sensor_type_set:
//                break;
            case R.id.setting_density_set:
                int density = Constant.ParamDefault.DENSITY;
                try {
                    density = Integer.parseInt(mInfo.getDensity());
                }
                catch (Exception e){
                }
                showSeekbarDialog("Density", 15, density, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int density = (Integer) v.getTag();
                        controller.setup(controller.getSetupDensityCommand(density) , new PrinterController.OnPrintCompletedListener() {
                            @Override
                            public void onCompleted(boolean isSuccess, String message) {
                                if(isSuccess) {
                                    ((BaseActivity)mContext).showProgress(null);
                                    controller.addCommandQueue(PrinterController.COMMAND_DENSITY);
                                }
                            }
                        });
                    }
                });
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResult:" + resultCode + ", " + requestCode);
        if(MainActivity.REQUEST_MEDIA_SIZE_CHANGE == requestCode) {
            ((BaseActivity)mContext).showProgress(null);
            PrinterController.getInstance(mContext).addCommandQueue(PrinterController.COMMAND_WIDTH);
            PrinterController.getInstance(mContext).addCommandQueue(PrinterController.COMMAND_HEIGHT);
            PrinterController.getInstance(mContext).addCommandQueue(PrinterController.COMMAND_SENSOR);
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
