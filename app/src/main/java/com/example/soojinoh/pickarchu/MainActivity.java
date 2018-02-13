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

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {

    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA"};
    private final static int PERMISSION_REQUEST_STORAGE = 0;
    private final int GALLERY_CODE=1112;
    private String selectedImagePath;

    private Camera mCamera = null;
    private CameraView mCameraView = null;
    DisplayMetrics dm;

    TextView capturetest;
    TextView gallerytest;
    // 아래 메뉴 접고 펼치는 버튼
    ImageView imgButton;
    RelativeLayout relativeLayout;
    FrameLayout frameLayout;
    ImageView imageView1, imageView2, imageView3, imageView4;
    ImageButton imgClose;

    boolean isMenuVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        initialize();

        findViewById(R.id.imageView).setOnTouchListener(this);
        findViewById(R.id.imageView).getRootView().setOnDragListener(this);

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int height = frameLayout.getHeight();
                if(isMenuVisible) {
                    imgButton.setImageResource(R.mipmap.button_1);
                    imgButton.setY(height - 250);
                    relativeLayout.setVisibility(View.GONE);
                    isMenuVisible = false;
                }
                else {
                    imgButton.setImageResource(R.mipmap.button_2);
                    imgButton.setY(height - 530);
                    relativeLayout.setVisibility(View.VISIBLE);
                    isMenuVisible = true;
                }
            }
        });

        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_CODE);
            }
        });
    }

    public void initialize() {
        capturetest = (TextView) findViewById(R.id.textView_capture_save_test);
        gallerytest = (TextView) findViewById(R.id.textView_call_gallery_test);
        frameLayout = (FrameLayout) findViewById(R.id.activity_main);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        imgButton = (ImageView) findViewById(R.id.button);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        imageView4 = (ImageView) findViewById(R.id.imageView4);
        imgClose = (ImageButton)findViewById(R.id.imgClose);
        dm = getApplicationContext().getResources().getDisplayMetrics();
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
        /*Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);*/
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
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        return cursor.getString(column_index);
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(null, shadowBuilder, view, 0);
            view.setVisibility(View.GONE);
            return true;
        } else {
            return false;
        }
    }

    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                float X = event.getX();
                float Y = event.getY();

                Log.d("Logcat", "X " + (int) X + "Y " + (int) Y);
                View view = (View) event.getLocalState();
                view.setX(X - (view.getWidth() / 2));
                view.setY(Y - (view.getHeight() / 2));
                view.setVisibility(View.VISIBLE);
            default:
                break;
        }
        return true;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Log.d("TAG", "Permission always deny");
                }
                break;
        }
    }

    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions) {
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}
