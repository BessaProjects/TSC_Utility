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

    public static Bitmap getBitmapFromPath(Context context, String path, int mode, boolean withScale){
        int width, height;
        float scale = 1;
        MediaInfo mediaInfo = ((BaseActivity)context).getDefaultMediaInfo();
        if(mediaInfo != null)
            scale = (float)mediaInfo.getWidth() / (float)mediaInfo.getHeight();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Object[] result = ImgUtil.replaceColor( BitmapFactory.decodeFile(path, options), Color.TRANSPARENT, Color.WHITE);
        Bitmap bitmap = (Bitmap) result[2];
        int pageWidth = (int)result[0], pageHeight = (int)result[1];

        if(mode == PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY) {
            width = DeviceUtil.getDisplay(context)[0];
            if(withScale)
                height = (int)(width / scale);
            else
                height = (int) ((float) width / ((float) pageWidth / (float) pageHeight));
        }
        else {
            width = pageWidth;
            if(withScale)
                height = (int)(width / scale);
            else
                height = pageHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static Bitmap getBitmapFromPdf(Context context, PdfRenderer pdfRenderer, int index, int mode, boolean withScale){
        PdfRenderer.Page page = pdfRenderer.openPage(index);

        int width, height;
        float scale = 1;
        MediaInfo mediaInfo = ((BaseActivity)context).getDefaultMediaInfo();
        if(mediaInfo != null)
            scale = (float)mediaInfo.getWidth() / (float)mediaInfo.getHeight();

        Bitmap bitmap;
        if(mode == PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY) {
            width = DeviceUtil.getDisplay(context)[0];
            height = (int) ((float) width / ((float) page.getWidth() / (float) page.getHeight()));
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        else{
            width = page.getWidth();
            height = page.getHeight();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        page.render(bitmap, new Rect(0, 0, width, height), null, mode);
        page.close();

        Object[] result = ImgUtil.replaceColor(bitmap, Color.TRANSPARENT, Color.WHITE);
        bitmap = (Bitmap) result[2];

        int pageWidth = (int)result[0], pageHeight = (int)result[1];

        if(mode == PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY) {
            width = DeviceUtil.getDisplay(context)[0];
            if(withScale)
                height = (int)(width / scale);
            else
                height = (int) ((float) width / ((float) pageWidth / (float) pageHeight));
        }
        else {
            width = pageWidth;
            if(withScale)
                height = (int)(width / scale);
            else
                height = pageHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static Object[] replaceColor(Bitmap bitmap, int fromColor, int targetColor) {
        if(bitmap == null) {
            return null;
        }
        Object[] obj = new Object[3];
        int width = bitmap.getWidth(), maxW = 0;
        int height = bitmap.getHeight(), maxH = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int h = 0; h < height; h++){
            for(int w = 0; w < width; w++){
                int px = pixels[h * width + w];

                pixels[h * width + w] = (px == fromColor) ? targetColor : px;

                if(pixels[h * width + w] != Color.WHITE){
                    if(maxW < w){
                        maxW = w;
                    }
                    if(maxH < h){
                        maxH = h;
                    }
                }
            }
        }
        Bitmap newImage = Bitmap.createBitmap(width, height, bitmap.getConfig());
        newImage.setPixels(pixels, 0, width, 0, 0, width, height);
        bitmap.recycle();

        obj[0] = maxW;
        obj[1] = maxH;
        obj[2] = Bitmap.createBitmap(newImage, 0, 0, maxW, maxH);
        return obj;
    }
}