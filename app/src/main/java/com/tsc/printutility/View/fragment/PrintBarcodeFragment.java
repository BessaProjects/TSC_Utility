package com.tsc.printutility.View.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tsc.printutility.Constant;
import com.tsc.printutility.Controller.PrinterController;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.PrefUtil;
import com.tsc.printutility.View.BaseActivity;
import com.tsc.printutility.View.MainActivity;
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
    TextView mType;
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

    @OnClick({R.id.print_barcode_action, R.id.print_barcode_media_size, R.id.print_barcode_type})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.print_barcode_action:
                if(((BaseActivity)mContext).isConnected()){
                    PrinterController.getInstance(mContext).printBarcode(((BaseActivity) mContext).getConnectIp(), mValue.getText() + "",
                            mType.getText() + "", new PrinterController.OnPrintCompletedListener() {
                                @Override
                                public void onCompleted(boolean isSuccess, String message) {
                                    String address = "";
                                    if(Constant.DeviceType.BLUETOOTH.equals(PrefUtil.getStringPreference(mContext, Constant.Pref.LAST_CONNECTED_DEVICE)))
                                        address = PrefUtil.getStringPreference(mContext, Constant.Pref.DEVICE_BT_NAME) + "\n";
                                    address += ((BaseActivity) mContext).getConnectIp();
                                    ((BaseActivity)mContext).showDataTransferDialog(address);
                                }
                            });
                }
                else {
                    new MaterialDialog.Builder(mContext)
                            .cancelable(false)
                            .content("Nothing connected. Go to connect page.")
                            .positiveText(R.string.general_go).onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ((MainActivity) mContext).gotoFragment(MainActivity.FragmentPage.PAGE_CONNECT);
                            dialog.dismiss();
                        }
                    }).negativeText(R.string.general_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                break;
            case R.id.print_barcode_media_size:
                Intent i = new Intent(mContext, MediaSettingActivity.class);
                startActivity(i);
                break;
            case R.id.print_barcode_type:
                String[] typeList = mContext.getResources().getStringArray(R.array.barcode_print_type_list);
                new MaterialDialog.Builder(mContext)
                        .items(typeList)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                mType.setText(text);
                                return false;
                            }
                        })
                        .negativeText(R.string.general_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
        }
    }
}
