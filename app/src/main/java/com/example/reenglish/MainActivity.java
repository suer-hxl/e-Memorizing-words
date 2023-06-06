package com.example.reenglish;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;


import android.annotation.SuppressLint;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.reenglish.tools.DatabaseHelper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {




    private DatabaseHelper databaseHelper;

    private static String pr_state;
    private static String back_state;
    private static String fr_state;
    public static int  res_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //创建视图
        super.onCreate(savedInstanceState);
        //设置使用视图
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);

        //读设置
        read_set();
        //设置随机背景
        random_back();

        //显示余额
        TextView qiebi = findViewById(R.id.qiebi);
        String money= get_money("user","money");
        qiebi.setText(money);
        //展示今天日期(签到card)
        show_date();


            //查询签到状态是否为空
            boolean qiandao_state=check_daily_state();
            if (qiandao_state){
                //隐藏签到
                View qiandaoView = findViewById(R.id.qiandao);
                qiandaoView.setVisibility(View.GONE);
            }



        //欢迎回来
            Toast.makeText(MainActivity.this, "欢迎回来", Toast.LENGTH_LONG).show();

        de_data();

}

    //删除数据库
    private void de_data(){
        String filePath = getFilesDir().getPath() + "/reenglishv1.db";
        File file = new File(filePath);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d("File Deletion", "Deleted file: " + filePath);
            } else {
                Log.e("File Deletion", "Failed to delete file: " + filePath);
            }
        } else {
            Log.d("File Existence", "File does not exist: " + filePath);
        }


    }

//随机背景
    private void random_back(){
        @SuppressLint("WrongViewCast") ImageView bg=findViewById(R.id.main_imageView_backg);
        if (back_state.equals("0"))
        {
            Log.d("TAG", "random_back: "+back_state);
                bg.setImageResource(R.drawable.a25);
                res_id = R.drawable.a25;
        }
        else {
            int [] img_res={
                    R.drawable.a1,
                    R.drawable.a2,
                    R.drawable.a3,
                    R.drawable.a4,
                    R.drawable.a5,
                    R.drawable.a6,
                    R.drawable.a7,
                    R.drawable.a8,
                    R.drawable.a9,
                    R.drawable.a10,
                    R.drawable.a11,
                    R.drawable.a12,
                    R.drawable.a13,
                    R.drawable.a14,
                    R.drawable.a15,
                    R.drawable.a16,
                    R.drawable.a17,
                    R.drawable.a18,
                    R.drawable.a19,
                    R.drawable.a20,
                    R.drawable.a21,
                    R.drawable.a22,
                    R.drawable.a23,
                    R.drawable.a24,
                    R.drawable.a25,
                    R.drawable.a26,
                    R.drawable.a27,
                    R.drawable.a28
            };
            res_id = img_res[rod()];
            bg.setImageResource(res_id);

        }
    }


    //查询余额
    @SuppressLint("Range")
    private String get_money(String TABLE , String COLUMN) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT "+ COLUMN +" FROM "+TABLE ;
        Cursor cursor = db.rawQuery(query, null);


        String Word = "";


        if (cursor.moveToFirst()) {
            Word = cursor.getString(cursor.getColumnIndex(COLUMN));
            Log.d("MainActivity", "getmoney: "+Word);
        }

        cursor.close();
        db.close();
        return Word;
    }
    //读取配置文件
    private void read_set(){
        try {
            File file = new File(getFilesDir(), "set.json");
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            isr.close();
            fis.close();

            String fileContent = sb.toString();

            Gson gson = new Gson();
            Person.SetData setData = gson.fromJson(fileContent, Person.SetData.class);

            pr_state = setData.phonetic;
            fr_state = setData.frequency;
            back_state = setData.background;
            Log.d("TAG", "read_set: "+pr_state);
            Log.d("TAG", "read_set: "+fr_state);
            Log.d("TAG", "read_set: "+back_state);



            // 使用 phonetic、frequency 和 background 进行相应的操作

        } catch (IOException e) {
            e.printStackTrace();
        }}
    @Override
    protected void onResume() {
        super.onResume();
        // 执行刷新操作，例如重新加载数据或更新UI元素
        read_set();
        random_back();
    }

    //展示签到日期
    private void show_date(){
        // 找到对应的 TextView 控件
        TextView dateTextView = findViewById(R.id.date);

        // 创建日期格式化对象
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 获取当前日期
        Date currentDate = new Date();

        // 格式化日期为字符串
        String formattedDate = dateFormat.format(currentDate);

        // 将格式化后的日期设置为 TextView 的文本内容
        dateTextView.setText(formattedDate);
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

    //更新日常状态_主页
    private void insert_daliy(){

        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String sql = "INSERT INTO daily (everyday,qiandao_state) VALUES ("+getCurrentDate()+",0)";


        db.execSQL(sql);

        // 关闭数据库连接
        db.close();

    }
    // 获取当前日期，并按照指定格式转换为字符串,比如20230531
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());}

@SuppressLint("Range")
private boolean check_daily_state() {
        boolean isFirstTime = true;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String tableName = "user"; // daily表名
        String columnName = "qiandao_state"; // qiandao_state列名
        String condition = " everyday="+getCurrentDate(); // 条件，判断是否是每天签到

        String query = "SELECT " + columnName + " FROM " + tableName + " WHERE " + condition;
        Cursor cursor = db.rawQuery(query, null);
        String   qiandaoState = "";
        if (cursor != null && cursor.moveToFirst()) {
            qiandaoState = cursor.getString(cursor.getColumnIndex(columnName));
            // 使用qiandaoState进行后续操作
            // ...
            cursor.close();
        }

        if (qiandaoState.equals("0")){
            isFirstTime = false;
            cursor.close();
        }

        if (cursor.getCount() == 0) {
                isFirstTime = false;//没查询到结果=没签到
                insert_daliy();//插入qiandao_state=0,当前日期
            cursor.close();
        }

        db.close();

        return isFirstTime;
    }




    //查询daily表qiandao_state字段值的数量
private String check_qiandao_tianshu() {
    String day ;
    SQLiteDatabase db = databaseHelper.getReadableDatabase();

    String query = "SELECT qiandao_state FROM user where qiandao_state=1";
    @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, null);

    day= String.valueOf(cursor.getCount());
    Log.d("TAG", "check_qiandao_tianshu: "+day);


    db.close();

    return day;
}


    //签到
    @SuppressLint("SetTextI18n")
    public void qiandao(View view) {
        update_yue();//金币+10
        update_daily();//金币+10
        //查询鹅币并显示
        TextView qiebi = findViewById(R.id.qiebi);
        String money= get_money("user","money");
        qiebi.setText(money);

        // 获取对 控件的引用
        View qiandaoView = findViewById(R.id.qiandao); //签到
        View qiandaocgView = findViewById(R.id.qiandaochenggong);//签到成功
        TextView qiandao_day_number=findViewById(R.id.qiandaodaynum);//签到天数
        // 隐藏 "qiandao" 控件
        qiandaoView.setVisibility(View.GONE);
        //显示qdcg控件
        qiandaocgView.setVisibility(View.VISIBLE);
        //设置签到天数
        qiandao_day_number.setText(check_qiandao_tianshu()+"天");

        // 延迟执行qdcg隐藏操作
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 将 "qiandaochenggong" 控件设置为不可见
                qiandaocgView.setVisibility(View.GONE);
            }
        }, 1000); // 延迟时间，单位为毫秒


    }

    private void update_daily() {
        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getReadableDatabase();


        String updateQuery = "INSERT INTO  user (qiandao_state,everyday) VALUES (1,+ "+getCurrentDate()+")";
        db.execSQL(updateQuery);

        // 关闭数据库连接
        db.close();
    }


    //跳转按钮
    @SuppressLint("ClickableViewAccessibility")
    public void learn(View view) {

        boolean re=get_wordbook_state();
        if (re){
        //跳转活动:
        Intent intent = new Intent(this, Learn.class);
            intent.putExtra("pr_state", pr_state);
            intent.putExtra("back_state", back_state);
            intent.putExtra("fr_state", fr_state);
            String r1e= String.valueOf(res_id);
            intent.putExtra("back_id", r1e);
            Log.d("TAG", "learn: "+r1e);
        // 启动目标活动
        startActivity(intent);}
        else {
            Toast.makeText(this, "没有需要学习的单词!", Toast.LENGTH_SHORT).show();
        }
    }

    public void fuxi(View view) {
        boolean re=getRiview("review");//获取review表的数据,如果为空则执行else
        Log.d("TAG", "fuxi: "+re);
        if (re){
        //跳转活动:

        Intent intent = new Intent(this, Fuxi.class);
            intent.putExtra("pr_state", pr_state);
            intent.putExtra("back_state", back_state);
            intent.putExtra("fr_state", fr_state);
            String r1e= String.valueOf(res_id);
            intent.putExtra("back_id", r1e);
        // 启动目标活动
        startActivity(intent);}
        else {
            Toast.makeText(this, "没有需要复习的单词!", Toast.LENGTH_SHORT).show();
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

    public void person(View view) {
        //跳转活动:
        Intent intent = new Intent(this, Person.class);

        // 启动目标活动
        startActivity(intent);
    }

    public void book(View view) {
        //跳转活动:
        Intent intent = new Intent(this, Book.class);

        // 启动目标活动
        startActivity(intent);
    }

    public void chart(View view) {
        //跳转活动:
        Intent intent = new Intent(this, Chart.class);
        intent.putExtra("pr_state", pr_state);
        intent.putExtra("back_state", back_state);
        intent.putExtra("fr_state", fr_state);
        String r1e= String.valueOf(res_id);
        intent.putExtra("back_id", r1e);
        // 启动目标活动
        startActivity(intent);


    }


    public int rod( ){
        Random random = new Random();
        int randomNumber = random.nextInt(28) ;
        return randomNumber;
    }




























}