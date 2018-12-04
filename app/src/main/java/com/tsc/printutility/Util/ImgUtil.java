package com.tsc.printutility.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;

import com.tsc.printutility.Entity.MediaInfo;
import com.tsc.printutility.View.BaseActivity;


public class ImgUtil {

    public static Bitmap getBitmapFromPdf(Context context, PdfRenderer pdfRenderer, int index, int mode){
        return getBitmapFromPdf(context, pdfRenderer, index, mode, true);
    }

    public static Bitmap getBitmapFromPdf(Context context, PdfRenderer pdfRenderer, int index, int mode, boolean withScale){
        PdfRenderer.Page page = pdfRenderer.openPage(index);

        int width, height;
        float scale = 1;
        MediaInfo mediaInfo = ((BaseActivity)context).getDefaultMediaInfo();
        if(mediaInfo != null)
            scale = (float)mediaInfo.getWidth() / (float)mediaInfo.getHeight();

        if(mode == PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY) {
            width = DeviceUtil.getDisplay(context)[0];
            if(withScale)
                height = (int)(width / scale);
            else
                height = (int) ((float) width / ((float) page.getWidth() / (float) page.getHeight()));
        }
        else {
            width = page.getWidth();
            if(withScale)
                height = (int)(width / scale);
            else
                height = page.getHeight();
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        System.out.println("getBitmapFromPdf:" + width + ", h:" + height);
        page.render(bitmap, new Rect(0, 0, width, height), null, mode);
        page.close();
        return ImgUtil.replaceColor(bitmap, Color.TRANSPARENT, Color.WHITE);
    }

    public static Bitmap getBitmapFromPath(Context context, String path, int mode, boolean withScale){
        int width, height;
        float scale = 1;
        MediaInfo mediaInfo = ((BaseActivity)context).getDefaultMediaInfo();
        if(mediaInfo != null)
            scale = (float)mediaInfo.getWidth() / (float)mediaInfo.getHeight();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(path, options);

        if(mode == PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY) {
            width = DeviceUtil.getDisplay(context)[0];
            if(withScale)
                height = (int)(width / scale);
            else
                height = (int) ((float) width / ((float) options.outWidth / (float) options.outHeight));
        }
        else {
            width = options.outWidth;
            if(withScale)
                height = (int)(width / scale);
            else
                height = options.outHeight;
        }

        options.inJustDecodeBounds = false;
        return ImgUtil.replaceColor(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(path, options), width, height, true), Color.TRANSPARENT, Color.WHITE);
    }

    public static Bitmap replaceColor(Bitmap bitmap, int fromColor, int targetColor) {
        if(bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int x = 0; x < pixels.length; ++x) {
            pixels[x] = (pixels[x] == fromColor) ? targetColor : pixels[x];
        }

        Bitmap newImage = Bitmap.createBitmap(width, height, bitmap.getConfig());
        newImage.setPixels(pixels, 0, width, 0, 0, width, height);
        bitmap.recycle();
        return newImage;
    }
}
