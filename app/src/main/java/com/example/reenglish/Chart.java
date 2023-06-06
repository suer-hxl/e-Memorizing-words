package com.example.reenglish;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.Date;
import java.util.Locale;

public class Chart extends AppCompatActivity {
    private DatabaseHelper databaseHelper;


    private static String pr_state;
    private static String fr_state;
    private static String res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        databaseHelper = new DatabaseHelper(this);

        //今日学习
        TextView today_learn=findViewById(R.id.show_word_number);
        today_learn.setText(getEverydayWordIdCount());
        //已学习
        TextView learned=findViewById(R.id.learn_state_1);
        learned.setText(get_learn_review("1"));
        //待学习
        TextView review=findViewById(R.id.learn_state_0);
        review.setText(get_learn_review("0"));
        //总共学习
        TextView total=findViewById(R.id.total);
        total.setText(get_total("daily","word_id"));
        //笔记
        TextView note=findViewById(R.id.note);
        note.setText(get_total("Note_TB","note"));
        //收藏
        TextView start=findViewById(R.id.start);
        start.setText(get_total("user","start"));

        Intent intent = getIntent();
        pr_state = intent.getStringExtra("pr_state");
        fr_state = intent.getStringExtra("fr_state");
        res = intent.getStringExtra("back_id");

    }
    private String get_total( String table,String column  ){

            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String query = "SELECT COUNT ("+column+") FROM "+table;
            Cursor cursor = db.rawQuery(query, null);
            String count = "";
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getString(0); // 获取第一列的值，即数量
            }

            if (cursor != null) {
                cursor.close();
            }
            if (cursor==null){
                count="0";
            }

            db.close();

            return count;
        }


    private String get_learn_review(String type){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT COUNT (state) FROM wordbook  WHERE state="+type;
        Cursor cursor = db.rawQuery(query, null);
        String count = "";
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getString(0); // 获取第一列的值，即数量
        }

        if (cursor != null) {
            cursor.close();
        }
        if (cursor==null){
            count="0";
        }

        db.close();

        return count;
    }
    private String getEverydayWordIdCount() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String tableName = "daily"; // daily表名
        String columnName = "word_id"; // word_id列名
        String condition = "everyday ="+ getCurrentDate(); // 条件，判断是否是2023年

        String query = "SELECT COUNT(" + columnName + ") FROM " + tableName + " WHERE " + condition;
        Cursor cursor = db.rawQuery(query, null);

        String count = "";
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getString(0); // 获取第一列的值，即数量
        }

        if (cursor != null) {
            cursor.close();
        }
        if (cursor==null){
            count="0";
        }

        db.close();

        return count;
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());}


    //返回
    public void chart_back(View view) {
        ImageView imageView = findViewById(R.id.chart_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    //跳转复习
    public void review(View view) {
        boolean re=getRiview("review");//获取review表的数据,如果为空则执行else
        Log.d("TAG", "fuxi: "+re);
        if (re){
            //跳转活动:
            Intent intent = new Intent(this, Fuxi.class);
            intent.putExtra("pr_state", pr_state);
            intent.putExtra("fr_state", fr_state);
            intent.putExtra("back_id", res);
            // 启动目标活动
            startActivity(intent);}
        else {
            Toast.makeText(this, "没有需要复习的单词!", Toast.LENGTH_SHORT).show();
        }
    }
    //判断是否能跳转
    private boolean getRiview(String tableName) {
        boolean isNull = false; // 默认为空
        String columnName =
                "(one_state=0 and one<="+getCurrentDate() +")or"+
                        "(two_state=0 and two<="+getCurrentDate() +")or"+
                        "(three_state=0 and three<="+getCurrentDate() +")or"+
                        "(four_state=0 and four<="+getCurrentDate() +")or"+
                        "(five_state=0 and five<="+getCurrentDate() +")";


        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT * FROM " + tableName + " WHERE (" + columnName +") ";
        Cursor cursor = db.rawQuery(query, null);
        Log.d("TAG", "getRiview: "+cursor.getCount()+"今天日期:"+getCurrentDate());
        if (cursor.getCount() > 0) {
            isNull = true; // 非空
            cursor.close();
        }

        db.close();
        Log.d("TAG", "判断有无: "+isNull);

        return isNull;
    }

    //跳转学习

    //判断是否能跳转
    private boolean get_wordbook_state() {
        boolean isNull = true; //默认不为空

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT state FROM  wordbook  WHERE  state =0 ";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() == 0) {
            isNull = false; // 空
        }
        cursor.close();


        db.close();
        Log.d("TAG", "get_wordbook_state: "+isNull);

        return isNull;
    }


    public void learn_chart(View view) {
        boolean re=get_wordbook_state();
        if (re){
            //跳转活动:
            Intent intent = new Intent(this, Learn.class);
            intent.putExtra("pr_state", pr_state);
            intent.putExtra("fr_state", fr_state);
            intent.putExtra("back_id", res);
            // 启动目标活动
            startActivity(intent);}
        else {
            Toast.makeText(this, "没有需要学习的单词!", Toast.LENGTH_SHORT).show();
        }
    }
}