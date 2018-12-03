package com.tsc.printutility.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tsc.printutility.Constant;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.PrefUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaInfoAdapter extends RecyclerView.Adapter<MediaInfoAdapter.ViewHolder> {

    private Context mContext;
    private View.OnClickListener mListener;
    private List<MediaInfo> mData = new ArrayList<>();
    private HashMap<Integer, Boolean> mPickerMap = new HashMap<>();
    private long mCurrentId;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mParent;
        public TextView title;
        public RadioButton picker;

        public ViewHolder(View v) {
            super(v);

            mParent = v;
            title = v.findViewById(R.id.setting_media_name);
            picker = v.findViewById(R.id.setting_media_picker);
        }
    }

    public MediaInfoAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<MediaInfo> data) {
        mCurrentId = PrefUtil.getLongPreference(mContext, Constant.Pref.PARAM_MEDIA_ID, -1);
        mData = data;
        for(int i = 0; i < data.size(); i++){
            if(mCurrentId == mData.get(i).getId())
                mPickerMap.put(i, true);
            else
                mPickerMap.put(i, false);
        }

        if(mCurrentId == -1 && data.size() > 0) {
            mPickerMap.put(0, true);
            PrefUtil.setLongPreference(mContext, Constant.Pref.PARAM_MEDIA_ID, mData.get(0).getId());
        }
        notifyDataSetChanged();
    }

    private void pickMediaSetting(int position){
        for(int i = 0; i < mData.size(); i++){
            if(position == i)
                mPickerMap.put(i, true);
            else
                mPickerMap.put(i, false);
        }
        notifyDataSetChanged();
    }

    public MediaInfo getSelectMedia(){
        for(int i = 0; i < mData.size(); i++){
            if(mPickerMap.get(i))
                return mData.get(i);
        }
        return null;
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting_media, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(mData.get(position).getUnit() == MediaInfo.UNIT_IN)
            holder.title.setText(mData.get(position).getName() + "\nW:" + mData.get(position).getWidth() + "in , H:" + mData.get(position).getHeight() + "in");
        else
            holder.title.setText(mData.get(position).getName() + "\nW:" + mData.get(position).getWidth() + "mm , H:" + mData.get(position).getHeight() + "mm");

        if(mPickerMap.get(position))
            holder.picker.setChecked(true);
        else
            holder.picker.setChecked(false);

        holder.picker.setTag(mData.get(position));
        holder.picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMediaSetting(position);
                mListener.onClick(view);
            }
        });

        holder.mParent.setTag(mData.get(position));
        holder.mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMediaSetting(position);
                mListener.onClick(view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}