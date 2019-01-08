package com.tsc.printutility.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tsc.printutility.Adapter.MediaInfoAdapter;
import com.tsc.printutility.Constant;
import com.tsc.printutility.Controller.PrinterController;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.Sqlite.MediaInfoController;
import com.tsc.printutility.Util.PrefUtil;
import com.tsc.printutility.Widget.LinearLayoutColorDivider;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MediaSettingActivity extends AppCompatActivity {

	@BindView(R.id.setting_media_list)
	RecyclerView mList;
	@BindView(R.id.toolbar_right)
	View mBtnRight;
	@BindView(R.id.toolbar_title)
	TextView mTitle;

	private MediaInfoController mMediaInfoController;
	private MediaInfoAdapter mAdapter;

	private boolean mIsMediaChanged = false;
	private PrinterController mPrinterController;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		ButterKnife.bind(this);
		mBtnRight.setVisibility(View.GONE);
		mTitle.setText("Media List");

		mMediaInfoController = new MediaInfoController(this);
		mPrinterController = PrinterController.getInstance(MediaSettingActivity.this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mList.setLayoutManager(layoutManager);
		mAdapter = new MediaInfoAdapter(this);
		mList.setAdapter(mAdapter);
		mList.addItemDecoration(new LinearLayoutColorDivider(getResources(), android.R.color.darker_gray, 1, LinearLayoutManager.VERTICAL));

		mAdapter.setData(mMediaInfoController.getAll());
		mAdapter.setOnItemClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				MediaInfo info = ((MediaInfo) view.getTag());

				PrefUtil.setLongPreference(MediaSettingActivity.this, Constant.Pref.PARAM_MEDIA_ID, info.getId());
				String command = mPrinterController.getSetupSizeCommand((int)info.getWidth(), (int)info.getHeight(), 0);

				if(info.getSensorType().equals(MediaInfo.SENSOR_TYPE_BLACK))
					command = mPrinterController.getSetupSizeCommand((int)info.getWidth(), (int)info.getHeight(), 1);

				mPrinterController.setup(command, new PrinterController.OnPrintCompletedListener() {
					@Override
					public void onCompleted(boolean isSuccess, String message) {
						if(isSuccess) {
							mIsMediaChanged = true;
							Toast.makeText(MediaSettingActivity.this, R.string.media_size_setup_success, Toast.LENGTH_LONG).show();
						}
						else{
							Toast.makeText(MediaSettingActivity.this, R.string.media_size_setup_failed, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
	}

	@OnClick({R.id.toolbar_left, R.id.setting_delete, R.id.setting_edit, R.id.setting_new})
	public void onClick(View view){
		final MediaInfo info = mAdapter.getSelectMedia();
		switch (view.getId()){
			case R.id.toolbar_left:
				onBackPressed();
				break;
			case R.id.setting_delete:
				if(info != null) {
					new MaterialDialog.Builder(this)
							.title(R.string.delete)
							.content(getString(R.string.media_delete_check, info.getName()))
							.positiveText(R.string.general_ok).onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							dialog.dismiss();
							PrefUtil.setLongPreference(MediaSettingActivity.this, Constant.Pref.PARAM_MEDIA_ID, -1);
							mMediaInfoController.delete(info.getId());
							mAdapter.setData(mMediaInfoController.getAll());
						}
					}).negativeText(R.string.cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							dialog.dismiss();
						}
					}).show();

				}
				else
					Toast.makeText(this, R.string.media_pick_alert,  Toast.LENGTH_LONG).show();
				break;
			case R.id.setting_edit:
				if(info != null) {
					showMediaEditor(info, new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialogInterface) {
							mAdapter.setData(mMediaInfoController.getAll());
						}
					});
				}
				else
					Toast.makeText(this, R.string.media_pick_alert,  Toast.LENGTH_LONG).show();
				break;
			case R.id.setting_new:
				showMediaEditor(null, new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialogInterface) {
						mAdapter.setData(mMediaInfoController.getAll());
					}
				});
				break;
		}
	}

	@Override
	public void onStop(){
		super.onStop();
		if(mIsMediaChanged){
			setResult(RESULT_OK);
			finish();
		}
	}

	public void showMediaEditor(MediaInfo mediaInfo, final DialogInterface.OnDismissListener listener){
		final AlertDialog dialog =new AlertDialog.Builder(this).create();
		dialog.setCancelable(false);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		if(mediaInfo == null){
			mediaInfo = new MediaInfo();
			mediaInfo.setId(-1);
			mediaInfo.setUnit(MediaInfo.UNIT_IN);
			mediaInfo.setSensorType(MediaInfo.SENSOR_TYPE_GAP);
		}

		final View view = LayoutInflater.from(this).inflate(R.layout.dialog_mediainfo_editor, null);
		final EditText editName = view.findViewById(R.id.mediainfo_editor_name);
		final EditText editWidth = view.findViewById(R.id.mediainfo_editor_width);
		final EditText editHeight = view.findViewById(R.id.mediainfo_editor_height);
		final RadioButton unitIn = view.findViewById(R.id.mediainfo_editor_unit_in);
		final RadioButton unitMm = view.findViewById(R.id.mediainfo_editor_unit_mm);
		final RadioButton typeGap = view.findViewById(R.id.mediainfo_editor_type_gap);
		final RadioButton typeBlack = view.findViewById(R.id.mediainfo_editor_type_black);
		view.setTag(mediaInfo);

		if(mediaInfo != null){
			editName.setText(mediaInfo.getName());
			if(mediaInfo.getWidth() > 0)
				editWidth.setText(mediaInfo.getWidth() + "");
			if(mediaInfo.getHeight() > 0)
				editHeight.setText(mediaInfo.getHeight() + "");
			if(mediaInfo.getUnit() == MediaInfo.UNIT_IN) {
				unitIn.setChecked(true);
				unitMm.setChecked(false);
			}
			else{
				unitIn.setChecked(false);
				unitMm.setChecked(true);
			}

			if(mediaInfo.getSensorType().equals(MediaInfo.SENSOR_TYPE_GAP)){
				typeGap.setChecked(true);
				typeBlack.setChecked(false);
			}
			else{
				typeGap.setChecked(false);
				typeBlack.setChecked(true);
			}
		}

		typeGap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					typeBlack.setChecked(false);
				}
			}
		});

		typeBlack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					typeGap.setChecked(false);
				}
			}
		});

		unitIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					unitMm.setChecked(false);
				}
			}
		});

		unitMm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					unitIn.setChecked(false);
				}
			}
		});

		view.findViewById(R.id.dialog_edit_btn_left).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		view.findViewById(R.id.dialog_edit_btn_right).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MediaInfo info = (MediaInfo) view.getTag();

				if(editName.getText().length() == 0 || editWidth.getText().length() == 0 || editHeight.getText().length() == 0) {
					Toast.makeText(MediaSettingActivity.this, R.string.media_field_cannot_empty, Toast.LENGTH_LONG).show();
					return;
				}
				if(Double.parseDouble(editWidth.getText().toString()) == 0 || Double.parseDouble(editHeight.getText().toString()) == 0){
					Toast.makeText(MediaSettingActivity.this, R.string.media_size_cannot_zero, Toast.LENGTH_LONG).show();
					return;
				}

				info.setName(editName.getText() + "");
				info.setWidth(Double.parseDouble(editWidth.getText().toString()));
				info.setHeight(Double.parseDouble(editHeight.getText().toString()));

				if(typeGap.isChecked())
					info.setSensorType(MediaInfo.SENSOR_TYPE_GAP);
				else
					info.setSensorType(MediaInfo.SENSOR_TYPE_BLACK);

				info.setUpdateTime(getDateTime(System.currentTimeMillis()));
				if(unitMm.isChecked())
					info.setUnit(MediaInfo.UNIT_MM);
				else
					info.setUnit(MediaInfo.UNIT_IN);

				if(info.getId() == -1)
					mMediaInfoController.insert(info);
				else
					mMediaInfoController.update(info);
				listener.onDismiss(null);
				dialog.dismiss();
			}
		});

		dialog.setView(view);
		dialog.show();
	}

	public static String getDateTime(long time){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy / MM / dd HH:mm:ss");
		return sdf.format(time);
	}
}