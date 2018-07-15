package com.capsane.simplecamera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // TODO: 后台检测外置摄像头是否连接

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 每次打开程序，在本程序目录，新建一个文件夹，为当前时间
        Globals.initSaveDir(getExternalFilesDir(null));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager wm =(WindowManager)getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Log.e(TAG, "width*height: " + width + "*" + height);

        /*capsane: 获取摄像头数量*/
        int cameraCount = Camera.getNumberOfCameras();
        Toast.makeText(getApplicationContext(), "你的手机一共有" + cameraCount + "个摄像头", Toast.LENGTH_SHORT).show();
        Log.e("123", "camera count: " + cameraCount);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, OpenCVActivity.class);
                startActivity(intent);
            }
        });

        Button bottomButton = findViewById(R.id.bottom_button);
        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LaunchCameraActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
