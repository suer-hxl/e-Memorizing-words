package com.example.reenglish;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reenglish.tools.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

public class login  extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Handler mHandler;
    private static final int MSG_COPY_STARTED = 1;
    private static final int MSG_COPY_PROGRESS = 2;
    private static final int MSG_COPY_COMPLETED = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //创建视图
        super.onCreate(savedInstanceState);
        //设置使用视图
        setContentView(R.layout.login);


        databaseHelper = new DatabaseHelper(this);

            createFilesInInternalStorage();//创建ex文件夹
            copyDatabaseFromAssets();//加载数据库
            copyAssetsToInternalStorage();//加载文件








    }




    //返回检测
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("不注册可能会导致异常,确定关闭吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 关闭应用程序
                finishAffinity();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 关闭弹窗
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    //user插入name.email.password.money.date
    public void sub(View view) {
// 获取密码输入框的信息
        EditText passwordEditText = findViewById(R.id.editTextTextPassword);
        String password = passwordEditText.getText().toString();

// 获取电子邮件地址输入框的信息
        EditText emailEditText = findViewById(R.id.editTextTextEmailAddress);
        String email = emailEditText.getText().toString();

// 获取通用文本输入框的信息
        EditText editText = findViewById(R.id.editText);
        String text = editText.getText().toString();

        String date=getCurrentDate();
// 执行数据库插入操作
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", password); // 替换 "password_column_name" 为密码字段的列名
        values.put("email", email); // 替换 "email_column_name" 为电子邮件字段的列名
        values.put("username", text); // 替换 "text_column_name" 为文本字段的列名
        values.put("book", "学位"); // 替换 "text_column_name" 为文本字段的列名
        values.put("money", "0"); // 替换 "text_column_name" 为文本字段的列名
        values.put("date", date); // 替换 "text_column_name" 为文本字段的列名
        db.insert("user", null, values); // 替换 "user" 为您的用户表的表名
        db.close();

        //跳转活动:
        Intent intent = new Intent(this, MainActivity.class);
        // 启动目标活动
        startActivity(intent);
    }

    // 获取当前日期，并按照指定格式转换为字符串,比如20230531
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());}

    // 在应用的内部存储空间中创建文件夹和文件
    private void createFilesInInternalStorage() {
        String folderName = "set";

        File folder = new File(getFilesDir(), folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
    //复制数据库到内部空间
    public void copyDatabaseFromAssets() {
        try {
            InputStream inputStream = getApplicationContext().getAssets().open("reenglishv1.db");
            String outFileName = getApplicationContext().getDatabasePath("reenglishv1.db").getPath();
            OutputStream outputStream = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //复制Assets资源到内部空间,除了数据库
    private void copyAssetsToInternalStorage() {

        // 初始化 Handler
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_COPY_STARTED:
                        Toast.makeText(login.this, "首次加载,需要加载资源,请稍后...", Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_COPY_COMPLETED:
                        Toast.makeText(login.this, "加载完成!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        new Thread(() -> {
            AssetManager assetManager = getAssets();
            String[] files = null;
            try {
                files = assetManager.list("");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (files != null) {
                mHandler.sendEmptyMessage(MSG_COPY_STARTED); // 发送复制开始的消息
                for (int i = 0; i < files.length; i++) {
                    String filename = files[i];
                    if (!filename.endsWith(".sqlite")) {
                        InputStream in;
                        OutputStream out;
                        try {
                            in = assetManager.open(filename);
                            String outputPath = getFilesDir() + File.separator + filename;
                            out = new FileOutputStream(outputPath);
                            copyFile(in, out);
                            in.close();
                            out.flush();
                            out.close();

                            // 模拟复制过程耗时
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 删除原始的 .sqlite 文件
                        String filePath = getFilesDir() + File.separator + filename;
                        File file = new File(filePath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
                mHandler.sendEmptyMessage(MSG_COPY_COMPLETED); // 发送复制完成的消息
            }
        }).start();
    }

    //提供复制文件方法
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
