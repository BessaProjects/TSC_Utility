package com.tsc.printutility.View.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileChooser;
import com.tsc.printutility.Constant;
import com.tsc.printutility.R;
import com.tsc.printutility.Util.FileUtil;
import com.tsc.printutility.View.MainActivity;
import com.tsc.printutility.Widget.DbxChooser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import butterknife.OnClick;

public class PrintFileFragment extends BaseFragment {

    private static final int DBX_CHOOSER_REQUEST = 9999;
    private static final int LOCAL_CHOOSER_REQUEST = 9998;

    private static final String APP_KEY = "jx61xdj0wywf93z";

    private DbxChooser mChooser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_print_file);

        mChooser = new DbxChooser(APP_KEY);

        return mView;
    }

    @OnClick({R.id.print_file_local, R.id.print_file_dropbox})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.print_file_local:
                showFileChooser();
                break;
            case R.id.print_file_dropbox:
                mChooser.forResultType(DbxChooser.ResultType.FILE_CONTENT)
                        .launch(this, DBX_CHOOSER_REQUEST);
                break;
        }
    }

    private void showFileChooser() {
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.setType("*/*");
//        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent i2 = new Intent(mContext, FileChooser.class);
        i2.putExtra(Constants.SELECTION_MODE,Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
        startActivityForResult(i2, LOCAL_CHOOSER_REQUEST);

//        try {
//            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), LOCAL_CHOOSER_REQUEST);
//        } catch (android.content.ActivityNotFoundException ex) {
//            // Potentially direct the user to the Market with a Dialog
//            Toast.makeText(mContext, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DBX_CHOOSER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                DbxChooser.Result result = new DbxChooser.Result(data);

                String path = result.getLink().toString().replaceFirst("file://", "");
                try {
                    path = URLDecoder.decode(path,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                System.out.println("printFile path:" + path);
                if(path != null && (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".pdf"))){
                    Intent i = new Intent(mContext, MainActivity.class);
                    i.putExtra(Constant.Extra.FILE_PATH, path);
                    startActivity(i);
                }
                else{
                    Toast.makeText(mContext, "不支援列印此當按類型", Toast.LENGTH_LONG).show();
                }
            }
            else {
                // Failed or was cancelled by the user.
            }
        }
        else if(requestCode == LOCAL_CHOOSER_REQUEST){
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                System.out.println("printFile path uri:" + uri + ", " + uri.getPath());
                String path = FileUtil.getPath(mContext, uri);
                System.out.println("printFile path:" + path);
                if(path != null && (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".pdf"))){
                    Intent i = new Intent(mContext, MainActivity.class);
                    i.putExtra(Constant.Extra.FILE_PATH, path);
                    startActivity(i);
                }
                else{
                    Toast.makeText(mContext, "不支援列印此當按類型", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
