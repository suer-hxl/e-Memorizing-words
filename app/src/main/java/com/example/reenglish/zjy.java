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
import android.widget.TextView;
import android.widget.Toast;

import com.example.reenglish.tools.DatabaseHelper;

import java.util.Date;
import java.util.Locale;

public class zjy extends AppCompatActivity {
    String value;
    private DatabaseHelper databaseHelper;
    private String res_id;
    private static String fr_state;
    private static String pr_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zjy);

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        value = intent.getStringExtra("key");
        res_id = intent.getStringExtra("back_id");
        fr_state = intent.getStringExtra("fr_state");
        pr_state = intent.getStringExtra("pr_state");


        update_yue();
        show_(value);
    }

    private void show_(String value)
    {
        TextView learn=findViewById(R.id.choose_shwo111);
        if (value.equals("L")){
            //修改文本内容为学习
            learn.setText("恭喜本组单词学习完成!");
            boolean re=get_wordbook_state();//获取review表的数据,如果为空则执行else
            if (!re){
                View ag=findViewById(R.id.again_card);
                ag.setVisibility(View.GONE);
            }
        }else {
            //修改文本内容为复习
            learn.setText("恭喜本组单词复习完成!");
            boolean re=getRiview("review");
            if (!re){
                View ag=findViewById(R.id.again_card);
                ag.setVisibility(View.GONE);
            }

        }



    }
    //查询tablename表下colum字段结果,如果空返回flase,非空返回ture
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

    //有数据返回true 无返回false
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


    // 在目标活动 zjy 中获取参数

    public void again(View view) {
        Log.d("TAG", "again: "+value);
        if (value.equals("L")){

            Intent intent = new Intent(this, Learn.class);

            String r1e= String.valueOf(res_id);
            intent.putExtra("back_id", r1e);
            intent.putExtra("fr_state", fr_state);
            intent.putExtra("pr_state", pr_state);
            finish();


            // 启动目标活动
            startActivity(intent);
        }
        else {
        Intent intent = new Intent(this, Fuxi.class);
            String r1e= String.valueOf(res_id);
            intent.putExtra("back_id", r1e);
            intent.putExtra("fr_state", fr_state);
            intent.putExtra("pr_state", pr_state);
            finish();


            // 启动目标活动
        startActivity(intent);}
    }
    //更新余额
    private void update_yue(){
        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getReadableDatabase();


        String updateQuery = "UPDATE user SET money = money + 10 ";
        db.execSQL(updateQuery);

        // 关闭数据库连接
        db.close();

    }
    // 获取当前日期，并按照指定格式转换为字符串,比如20230531
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());}

    public void end(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
}