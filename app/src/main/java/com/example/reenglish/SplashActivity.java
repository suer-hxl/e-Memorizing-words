package com.example.reenglish;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 500; // 闪屏页的延迟时间，单位为毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        boolean folderExists = checkFolderExists("set");
        if (folderExists)//日常检测
            {
                // 延迟跳转到主页面
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 创建一个意图，跳转到主页面
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);

                        // 关闭当前的闪屏页
                        finish();
                    }
                }, SPLASH_DELAY);
            }
        else {

            // 延迟跳转到主页面
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 创建一个意图，跳转到注册页
                    Intent intent = new Intent(SplashActivity.this, login.class);
                    startActivity(intent);

                    // 关闭当前的闪屏页
                    finish();
                }
            }, SPLASH_DELAY);


        }


    }
    //检查内部目录是否有指定的文件夹
    private boolean checkFolderExists(String folderName) {
        File internalStorageDir = getFilesDir();
        File folder = new File(internalStorageDir, folderName);
        return folder.exists() && folder.isDirectory();
    }





}
