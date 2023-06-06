package com.example.reenglish;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reenglish.tools.DatabaseHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public class Book extends AppCompatActivity {
    String book,author,count;
    private DatabaseHelper databaseHelper;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);




        databaseHelper = new DatabaseHelper(this);
        getbook();

        String day=getDaysDifferenceFromToday();
        TextView wordTextday = findViewById(R.id.day);
        wordTextday.setText(day);

        setWordCount();





    }

    //查询注册天数和本机相差多少天
    private String getDaysDifferenceFromToday() {
        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT date FROM user";
        Cursor cursor = db.rawQuery(query, null);
        String daysDiff="";

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String userDateTimestamp = String.valueOf(cursor.getLong(cursor.getColumnIndex("date")));
            Log.d("TAG", "userDateTimestamp: "+userDateTimestamp);
            daysDiff = get_diff(userDateTimestamp,getCurrentDate());
        }


        String day1="总共学习: "+daysDiff+"天";

        cursor.close();
        db.close();

        return day1;
    }
    // 获取当前日期，并按照指定格式转换为字符串,比如20230531
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());}

    private static String get_diff(String day1, String day2) {
        // 解析日期字符串为 LocalDate 对象
        LocalDate date1 = LocalDate.parse(day1, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate date2 = LocalDate.parse(day2, DateTimeFormatter.BASIC_ISO_DATE);

        // 计算日期差异
        String day=String.valueOf(ChronoUnit.DAYS.between(date1, date2));
        Log.d("TAG", "get_diff: "+day);
        return day;
    }
    //查询学习单词数量
    @SuppressLint("Range")
    private void setWordCount() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) AS count FROM wordbook WHERE state = 1";
        Cursor cursor = db.rawQuery(query, null);

        int wordCount = 0;
        if (cursor.moveToFirst()) {
            wordCount = cursor.getInt(cursor.getColumnIndex("count"));
        }

        cursor.close();
        db.close();
        String cou="总共学习: "+wordCount+" 词";
        TextView wordCountTextView = findViewById(R.id.word1_count);
        wordCountTextView.setText(cou);
    }






    //设置书籍信息
    private void getbook() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT book FROM user";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            // 从结果中获取book的值
            @SuppressLint("Range") String book = cursor.getString(cursor.getColumnIndex("book"));

            // 判断book的名称并设置author和count的值
            switch (book) {
                case "学位":
                    book="成人本科词汇正序版";
                    author = "俞敏洪";
                    count = "3190";
                    setname(book,author,count);
                    break;
                case "四级":
                    book="全国等级考试词汇必备第四级";
                    author = "王俊";
                    count = "5676";
                    setname(book,author,count);
                    break;
                case "六级":
                    book="星火六级巧记速记（乱序版）";
                    author = "马德高";
                    count = "3017";
                    setname(book,author,count);
                    break;
            }
        }

        cursor.close();
        db.close();
    }
    private void setname(String b,String a,String c){
        TextView wordTextEn = findViewById(R.id.bookname);
        wordTextEn.setText(b);
        TextView wordText1 = findViewById(R.id.bookanthor);
        wordText1.setText(a);
        TextView wordText2 = findViewById(R.id.bookcount);
        wordText2.setText(c);

    }

    //展示选择框
    public void book(View view) {
            // 获取对 控件的引用
            View qiandaocgView = findViewById(R.id.show_choose);
            //显示qdcg控件
            qiandaocgView.setVisibility(View.VISIBLE);
    }


    //隐藏选择自定义词书控件
    public void back1(View view) {


        View qiandaocgView = findViewById(R.id.show_choose);

        qiandaocgView.setVisibility(View.GONE);

    }

    //选择对应的书籍   学位
    public void choose_xuewei(View view) {
        update_userbookname("学位");

        Toast.makeText(this, "切换成功!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Book.class);

        // 启动当前活动
        startActivity(intent);

        // 结束当前活动，使其被销毁
        finish();
    }
    //选择对应的书籍   四级
    public void choose_4(View view) {
        update_userbookname("四级");
        Toast.makeText(this, "切换成功!", Toast.LENGTH_SHORT).show();
        // 创建一个Intent对象，指定当前活动的类名作为目标
        Intent intent = new Intent(this, Book.class);

        // 启动当前活动
        startActivity(intent);

        // 结束当前活动，使其被销毁
        finish();

    }

    //选择对应的书籍   六
    public void choose_6(View view) {
        update_userbookname("六级");
        Toast.makeText(this, "切换成功!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Book.class);

        // 启动当前活动
        startActivity(intent);

        // 结束当前活动，使其被销毁
        finish();
    }
    //返回上一页
    public void back_main(View view) {
        ImageView imageView = findViewById(R.id.chart_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //更新书籍状态
    private void update_userbookname(String name) {
        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("book", name);

        db.update("user", values, null, null);

        // 关闭数据库连接
        db.close();
    }

}