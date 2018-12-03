package com.tsc.printutility.Util;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    public static String exportFile(Context context, ParcelFileDescriptor printdocument){
        String filename = "document.pdf";
        File outfile = new File(context.getFilesDir(), filename);
        outfile.delete();
        FileInputStream file = new ParcelFileDescriptor.AutoCloseInputStream(printdocument);
        int length = -1;
        // 创建一个长度为1024的内存空间
        byte[] bbuf = new byte[99999];
        // 用于保存实际读取的字节数
        // 使用循环来重复读取数据
        try {
            FileOutputStream outStream = new FileOutputStream(outfile);
            while ((length = file.read(bbuf)) != -1) {
                // 将字节数组转换为字符串输出
                // System.out.print(new String(bbuf, 0, hasRead));
//				outStream.write(bbuf);
                outStream.write(bbuf, 0, length);
            }
            outStream.close();
            return outfile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭文件输出流，放在finally块里更安全
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
