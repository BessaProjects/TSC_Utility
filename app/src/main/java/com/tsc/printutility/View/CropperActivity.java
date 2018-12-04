package com.tsc.printutility.View;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tsc.printutility.Constant;
import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.DeviceUtil;
import com.tsc.printutility.Util.ImgUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CropperActivity extends BaseActivity {

    private CropImageView mCropper;
    private PhotoView mPhotoView;

    private ImageView mBtnRight, mBtnLeft;

    private String mFilePath;
    private Bitmap mSource, mCropImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_cropper);

        mCropper = findViewById(R.id.cropImageView);
        mBtnRight = findViewById(R.id.toolbar_right);
        mBtnRight.setImageResource(R.drawable.icon_confirm);
        mBtnRight.setVisibility(View.VISIBLE);

        mBtnLeft = findViewById(R.id.toolbar_left);

        mPhotoView = findViewById(R.id.photoviewer_photo);

        mFilePath = getIntent().getStringExtra(Constant.Extra.FILE_PATH);
        int index = getIntent().getIntExtra(Constant.Extra.CROP_INDEX, 0);
        try {
            if(mFilePath.endsWith(".pdf")) {
                PdfRenderer pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(new File(mFilePath), ParcelFileDescriptor.MODE_READ_ONLY));
                mSource = ImgUtil.getBitmapFromPdf(this, pdfRenderer, index, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY, false);
                pdfRenderer.close();
            }
            else{
                mSource = ImgUtil.getBitmapFromPath(this, mFilePath, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY, false);
            }
            mCropper.setImageBitmap(mSource);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        mBtnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPhotoView.getVisibility() == View.GONE) {
                    onBackPressed();
                }
                else{
                    mPhotoView.setVisibility(View.GONE);
                    mBtnRight.setImageResource(R.drawable.icon_crop);
                }
            }
        });

        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPhotoView.getVisibility() == View.GONE) {
                    mBtnRight.setImageResource(R.drawable.icon_printer);
                    mPhotoView.setVisibility(View.VISIBLE);

                    MediaInfo info = getDefaultMediaInfo();
                    float scale = 1;
                    if(info != null)
                        scale = (float)info.getWidth() / (float)info.getHeight();
                    int width = DeviceUtil.getDisplay(CropperActivity.this)[0];
                    mCropImage = Bitmap.createScaledBitmap(mCropper.getCroppedImage(), width, (int)(width / scale), true);
                    mPhotoView.setImageBitmap(mCropImage);
                }
                else{
                    List<Bitmap> b = new ArrayList<>();
                    b.add(mCropper.getCroppedImage());
                    showDevicePicker(b);
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        try {
            if(mSource != null)
                mSource.recycle();
            if(mCropImage != null)
                mCropImage.recycle();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
