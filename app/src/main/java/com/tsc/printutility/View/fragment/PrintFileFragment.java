package com.tsc.printutility.View.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tsc.printutility.Constant;
import com.tsc.printutility.R;
import com.tsc.printutility.View.MainActivity;
import com.tsc.printutility.Widget.DbxChooser;

import java.net.URISyntaxException;

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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), LOCAL_CHOOSER_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(mContext, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DBX_CHOOSER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                DbxChooser.Result result = new DbxChooser.Result(data);
                Log.d("main", "Link to selected file: " + result.getLink());
                String path = result.getLink().toString();

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
                // Get the Uri of the selected file
                Uri uri = data.getData();
                // Get the path
                String path = null;//这里可能需要加个异常捕捉处理
                try {
                    path = getPath(mContext, uri);
                    Log.d("main", "Link to selected file: " + path);
                    if(path != null && (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".pdf"))){
                        Intent i = new Intent(mContext, MainActivity.class);
                        i.putExtra(Constant.Extra.FILE_PATH, path);
                        startActivity(i);
                    }
                    else{
                        Toast.makeText(mContext, "不支援列印此當按類型", Toast.LENGTH_LONG).show();
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

}
