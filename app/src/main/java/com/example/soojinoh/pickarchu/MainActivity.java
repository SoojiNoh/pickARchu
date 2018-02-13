package com.example.soojinoh.pickarchu;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final static int PERMISSION_REQUEST_STORAGE = 0;
    private final int GALLERY_CODE=1112;
    private String selectedImagePath;

    TextView capturetest;
    TextView gallarytest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capturetest = (TextView) findViewById(R.id.textView_capture_save_test);
        gallarytest = (TextView) findViewById(R.id.textView_call_gallary_test);
    }

    public void onCameraViewClick(View v) {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);

        Bitmap screenBitmap = view.getDrawingCache();
        File f = storeScreenShot(screenBitmap);
        if(f != null)
            openScreenshot(f);

        view.setDrawingCacheEnabled(false);
    }

    public void onMoreImageViewClick(View v) { // selectImageFromGallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
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

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_CODE:
                    selectedImagePath = getRealPathFromURI(data.getData());

                    Log.d("***sssss***", selectedImagePath);
                    break;
                default:
                    break;
            }

        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        return cursor.getString(column_index);
    }

}
