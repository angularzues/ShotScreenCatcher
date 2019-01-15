package com.example.shuanglingli.shotscreencatcher;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private ScreenshotObserver screenshotObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
    }

    private void checkPermissions(){

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        screenshotObserver = new ScreenshotObserver(this);
        screenshotObserver.setScreenshotListener(screenshotListener);
    }


    ScreenshotObserver.ScreenshotListener screenshotListener = new ScreenshotObserver.ScreenshotListener() {
        @Override
        public void onScreenshot(Uri contentUri) {
            Log.i("ScreenShot", "===========screenshot occur and uri =" + contentUri);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (screenshotObserver != null)
            screenshotObserver.removeScreenshotListener();
    }
}
