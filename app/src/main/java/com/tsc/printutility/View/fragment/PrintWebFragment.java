package com.tsc.printutility.View.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.tsc.printutility.Constant;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.ImgUtil;
import com.tsc.printutility.View.BaseActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;
import rebus.permissionutils.PermissionUtils;
import rebus.permissionutils.SimpleCallback;

public class PrintWebFragment extends BaseFragment {

    @BindView(R.id.photo_viewpager_index)
    TextView mPagerIndex;
    @BindView(R.id.photo_viewpager)
    ViewPager mPager;

    private String mFilePath;
    private PdfRenderer mPdfRenderer;
    private int mPageCount = 0;
    private PhotoPagerAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_print_web);

        mFilePath = getArguments().getString(Constant.Extra.FILE_PATH);
        try {
            mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(new File(mFilePath), ParcelFileDescriptor.MODE_READ_ONLY));
            mPageCount = mPdfRenderer.getPageCount();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        int selectedIndex = 0;
        mAdapter = new PhotoPagerAdapter();
        mPager.setAdapter(mAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPagerIndex.setText((position + 1) + "/" + mPageCount);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPager.setCurrentItem(selectedIndex);
        mPagerIndex.setText((selectedIndex + 1) + "/" + mPageCount);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        askForPermission();

        return mView;
    }


    @Override
    public void onResume(){
        super.onResume();

        mAdapter = new PhotoPagerAdapter();
        mPager.setAdapter(mAdapter);
    }

    public String getFilePath(){
        return mFilePath;
    }

    private void askForPermission(){
        if(!PermissionUtils.isGranted(mContext, PermissionEnum.WRITE_EXTERNAL_STORAGE)) {
            PermissionManager.Builder()
                    .permission(PermissionEnum.WRITE_EXTERNAL_STORAGE)
                    .callback(new SimpleCallback() {
                        @Override
                        public void result(boolean allPermissionsGranted) {
                            if (!allPermissionsGranted)
                                askForPermission();
                        }
                    })
                    .ask(this);
        }
    }

    @OnClick({R.id.photo_view_printer})
    public void onClick(View view){
        ((BaseActivity)mContext).hideKeyboard();
        switch (view.getId()){
            case R.id.photo_view_printer:
                ((BaseActivity)mContext).showDevicePicker(mPdfRenderer);
                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mPdfRenderer != null)
            mPdfRenderer.close();
    }

    private class PhotoPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPageCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view  = LayoutInflater.from(mContext).inflate(R.layout.item_photoviewer, null);
            PhotoView photoView = view.findViewById(R.id.photoviewer_photo);
            container.addView(view);

            photoView.setImageBitmap(ImgUtil.getBitmapFromPdf(mContext, mPdfRenderer, position, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY));
            return view;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer
    {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;
        @SuppressLint("NewApi")
        public void transformPage(View view, float position)
        {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) {
                view.setAlpha(0);

            } else if (position <= 1)
            {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                view.setAlpha(0);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onAc:" + requestCode);
    }

}
