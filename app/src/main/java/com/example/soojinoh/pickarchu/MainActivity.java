package com.example.soojinoh.pickarchu;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    final static int PERMISSION_REQUEST_STORAGE = 0;
    TextView view; //일단 카메라뷰가 없으므로 이걸로 대체 합니다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (TextView) findViewById(R.id.textView);
    }

    public void onCameraViewClick(View v) {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);

        Bitmap screenBitmap = view.getDrawingCache();
        File f = storeScreenShot(screenBitmap);
        if(f != null)
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));

        view.setDrawingCacheEnabled(false);
    }

    public File storeScreenShot(Bitmap screenBitmap) {
        checkPermission();
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyymmdd_hhmmss");
        String filename = "pic_" + sdfNow.format(new Date(System.currentTimeMillis())) +".jpg";
        String storage_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures";

        File file = new File(storage_path, filename);
        FileOutputStream os = null;
        try {
            String state = Environment.getExternalStorageState();
            if(Environment.MEDIA_UNMOUNTED.equals(state))
                throw new Exception("UnmountedException");
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (IOException ie) {
            Toast.makeText(getApplicationContext(), "저장공간이 부족합니다",Toast.LENGTH_SHORT);
            ie.printStackTrace();
            return null;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "저장장치가 없습니다",Toast.LENGTH_SHORT);
            e.printStackTrace();
            return null;
        }
        return file;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        }else{
            Log.d("TAG", "Permission already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Log.d("TAG", "Permission always deny");
                }
                break;
        }
    }
}
