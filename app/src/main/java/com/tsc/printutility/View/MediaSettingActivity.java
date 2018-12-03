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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tsc.printutility.Adapter.MediaInfoAdapter;
import com.tsc.printutility.Constant;
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

	private MediaInfoController mMediaInfoController;
	private MediaInfoAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		ButterKnife.bind(this);
		mBtnRight.setVisibility(View.GONE);

		mMediaInfoController = new MediaInfoController(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		mList.setLayoutManager(layoutManager);
		mAdapter = new MediaInfoAdapter(this);
		mList.setAdapter(mAdapter);
        mList.addItemDecoration(new LinearLayoutColorDivider(getResources(), android.R.color.darker_gray, 1, LinearLayoutManager.VERTICAL));

		mAdapter.setData(mMediaInfoController.getAll());
		mAdapter.setOnItemClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PrefUtil.setLongPreference(MediaSettingActivity.this, Constant.Pref.PARAM_MEDIA_ID, ((MediaInfo)view.getTag()).getId());
			}
		});

		onNewIntent(getIntent());
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
							.title("Delete")
							.content("Are you sure delete \"" + info.getName() + "\"")
							.positiveText("Ok").onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							dialog.dismiss();
							PrefUtil.setLongPreference(MediaSettingActivity.this, Constant.Pref.PARAM_MEDIA_ID, -1);
							mMediaInfoController.delete(info.getId());
							mAdapter.setData(mMediaInfoController.getAll());
						}
					}).negativeText("Cancel").onNegative(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							dialog.dismiss();
						}
					}).show();

				}
				else
					Toast.makeText(this, "請選擇一個Media",  Toast.LENGTH_LONG).show();
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
					Toast.makeText(this, "請選擇一個Media",  Toast.LENGTH_LONG).show();
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

	public void showMediaEditor(MediaInfo mediaInfo, final DialogInterface.OnDismissListener listener){
		final AlertDialog dialog =new AlertDialog.Builder(this).create();
		dialog.setCancelable(false);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		if(mediaInfo == null){
			mediaInfo = new MediaInfo();
			mediaInfo.setId(-1);
			mediaInfo.setName("New Media");
			mediaInfo.setWidth(Constant.ParamDefault.WIDTH);
			mediaInfo.setHeight(Constant.ParamDefault.HEIGHT);
			mediaInfo.setUnit(MediaInfo.UNIT_IN);
		}

		final View view = LayoutInflater.from(this).inflate(R.layout.dialog_mediainfo_editor, null);
		final EditText editName = view.findViewById(R.id.mediainfo_editor_name);
		final EditText editWidth = view.findViewById(R.id.mediainfo_editor_width);
		final EditText editHeight = view.findViewById(R.id.mediainfo_editor_height);
		final RadioButton unitIn = view.findViewById(R.id.mediainfo_editor_unit_in);
		final RadioButton unitMm = view.findViewById(R.id.mediainfo_editor_unit_mm);
		view.setTag(mediaInfo);

		if(mediaInfo != null){
			editName.setText(mediaInfo.getName());
			editWidth.setText(mediaInfo.getWidth() + "");
			editHeight.setText(mediaInfo.getHeight() + "");
			if(mediaInfo.getUnit() == MediaInfo.UNIT_IN) {
				unitIn.setChecked(true);
				unitMm.setChecked(false);
			}
			else{
				unitIn.setChecked(false);
				unitMm.setChecked(true);
			}
		}

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
					Toast.makeText(MediaSettingActivity.this, "欄位不可為空", Toast.LENGTH_LONG).show();
					return;
				}
				if(Double.parseDouble(editWidth.getText().toString()) == 0 || Double.parseDouble(editHeight.getText().toString()) == 0){
					Toast.makeText(MediaSettingActivity.this, "欄位不可為０", Toast.LENGTH_LONG).show();
					return;
				}

				info.setName(editName.getText() + "");
				info.setWidth(Double.parseDouble(editWidth.getText().toString()));
				info.setHeight(Double.parseDouble(editHeight.getText().toString()));
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