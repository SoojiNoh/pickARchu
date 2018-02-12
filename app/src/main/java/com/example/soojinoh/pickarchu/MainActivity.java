/*
 *  https://github.com/josnidhin/Android-Camera-Example에 있는 코드를 수정했습니다.
 *  http://webnautes.tistory.com/822
*/


package com.example.soojinoh.pickarchu;

import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnDragListener {

    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private TextureView mCameraTextureView;
    private Preview mPreview;
    DisplayMetrics dm;

    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA"};

    ImageView imgButton;
    ImageView imgButton2;
    RelativeLayout relativeLayout;
    FrameLayout frameLayout;
    ImageView imageView1, imageView2, imageView3, imageView4;

    boolean isMenuVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.imageView).setOnTouchListener(this);
        findViewById(R.id.imageView).getRootView().setOnDragListener(this);

        dm = getApplicationContext().getResources().getDisplayMetrics();
        mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        mPreview = new Preview(this, mCameraTextureView);

        frameLayout = (FrameLayout) findViewById(R.id.activity_main);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        imgButton = (ImageView) findViewById(R.id.button);
        //imgButton2 = (ImageView) findViewById(R.id.button2);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        imageView4 = (ImageView) findViewById(R.id.imageView4);



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

        //btn to close the application
        ImageButton imgClose = (ImageButton)findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

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
                    //imageView.setVisibility(View.VISIBLE);
                }
            }
        });


        Log.d("****Camera Actv","onCreate 실행");
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

    @Override
    protected void onResume() {
        super.onResume();
        mPreview.onResume();

        Log.d("****Camera Actv", "onResume 실행");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
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
        }
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