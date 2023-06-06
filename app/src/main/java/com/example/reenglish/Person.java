package com.example.reenglish;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.reenglish.tools.DatabaseHelper;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;


public class Person extends AppCompatActivity {
    private DatabaseHelper databaseHelper;

    private int uk_state,us_state,ten_state,five_state,back_state;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        set_background();//设置背景渐变'
        databaseHelper = new DatabaseHelper(this);

        //昵称
        TextView username=findViewById(R.id.username);
        username.setText("昵称:"+get_total("user","username"));
        //账号
        TextView account=findViewById(R.id.account);
        account.setText("账号:"+get_total("user","email"));
        //计算注册天数和当前天数
        TextView total_day=findViewById(R.id.total_day);
        total_day.setText("至今已使用:"+getDaysDifferenceFromToday());
        //余额
        TextView yu_e=findViewById(R.id.yu_e);
        String money= get_money("user","money");
        yu_e.setText("余额:"+money);
        read_set();


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
            SetData setData = gson.fromJson(fileContent, SetData.class);

            String phonetic = setData.phonetic;
            String frequency = setData.frequency;
            String background = setData.background;
            Log.d("TAG", "read_set: "+phonetic);
            Log.d("TAG", "read_set: "+frequency);
            Log.d("TAG", "read_set: "+background);

            // 使用 phonetic、frequency 和 background 进行相应的操作
            show_switch(phonetic,frequency,background);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private String readSetJsonFile() throws IOException {
        // 读取set.json文件内容并返回
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(getFilesDir(), "set.json"));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveSetJsonFile(String jsonContent) throws IOException {
        // 将jsonContent保存到set.json文件中
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(getFilesDir(), "set.json"));
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(jsonContent);
            bw.flush();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void ten(View view) throws JSONException, IOException {

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch tenSwitch = findViewById(R.id.ten);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch five = findViewById(R.id.five);
        chage(tenSwitch,five,"frequency","10","5");

    }
    public void five(View view) throws JSONException, IOException {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch five=findViewById(R.id.five);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch ten=findViewById(R.id.ten);

        chage(five,ten,"frequency","5","10");
    }
    private void chage(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch name1,Switch name2, String key, String value, String value1) throws IOException, JSONException {

        boolean is_checked=name1.isChecked();
        try {
            String jsonContent=readSetJsonFile();

            JSONObject jsonObject=new JSONObject(jsonContent);
            if (is_checked){
                jsonObject.put(key,value);
                name2.setChecked(false);
            }else {
                jsonObject.put(key,value1);
                name2.setChecked(true);
            }

            String updated=jsonObject.toString();
            Log.d("updated", "updated: "+updated);

            String updateed=jsonObject.toString();

            saveSetJsonFile(updateed);



        }catch (IOException | JSONException e)
        {
            e.printStackTrace();
        }

    }

    public void uk(View view) throws JSONException, IOException {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch uk=findViewById(R.id.uk);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch us=findViewById(R.id.us);


        chage(uk,us,"phonetic","uk","us");
    }

    public void us(View view) throws JSONException, IOException {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch uk=findViewById(R.id.uk);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch us=findViewById(R.id.us);
        chage(us,uk,"phonetic","us","uk");
    }

    public void back_ground(View view) throws JSONException, IOException {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch bbbb=findViewById(R.id.random_background);
        boolean isChecked = bbbb.isChecked();

        try {
            // 读取set.json文件内容
            String jsonContent = readSetJsonFile();

            // 解析JSON数据为JSONObject
            JSONObject jsonObject = new JSONObject(jsonContent);

            // 修改frequency字段的值
            if (isChecked) {
                jsonObject.put("background", "1");
                bbbb.setChecked(true);
            } else {
                jsonObject.put("background", "0");
                bbbb.setChecked(false);
            }

            // 将修改后的JSONObject转换为字符串
            String updatedJson = jsonObject.toString();

            // 保存更新后的JSON字符串到set.json文件
            saveSetJsonFile(updatedJson);

            // 输出更新后的结果
            Log.d("SetData", "Updated SetData: " + updatedJson);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //接收参数
    public static class SetData {
        public String phonetic;
        public String frequency;
        public String background;

    }
    //判断展示switch开关
    private void show_switch(String phonetic,String frequency,String background){
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch uk=findViewById(R.id.uk);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch us=findViewById(R.id.us);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch back=findViewById(R.id.random_background);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch ten=findViewById(R.id.ten);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch five=findViewById(R.id.five);

        if (phonetic.equals("uk")){
            uk.setChecked(true);
            us.setChecked(false);
            uk_state=1;
            us_state=0;
        }else {
            uk.setChecked(false);
            us.setChecked(true);
            uk_state=0;
            us_state=1;
        }

        if (background.equals("1")) {
            back.setChecked(true);
            back_state=1;
        } else {
            back.setChecked(false);
            back_state=0;
        }

        if (frequency.equals("10")){
            ten.setChecked(true);
            five.setChecked(false);
            ten_state=1;
            five_state=0;
        }else {
            ten.setChecked(false);
            five.setChecked(true);
            ten_state=0;
            five_state=1;
        }

    }



    //获取信息对应表的字段值
    private String get_total( String table,String column  ){

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT  "+column+" FROM "+table;
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


        String day1=daysDiff+"天";

        cursor.close();
        db.close();

        return day1;
    }
    private static String get_diff(String day1, String day2) {
        // 解析日期字符串为 LocalDate 对象
        LocalDate date1 = LocalDate.parse(day1, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate date2 = LocalDate.parse(day2, DateTimeFormatter.BASIC_ISO_DATE);

        // 计算日期差异
        String day=String.valueOf(ChronoUnit.DAYS.between(date1, date2));
        Log.d("TAG", "get_diff: "+day);
        return day;
    }
    // 获取当前日期，并按照指定格式转换为字符串,比如20230531
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());}

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
        return Word;}
    //返回按钮
    public void back_main(View view) {

        ImageView imageView = findViewById(R.id.back_person);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    //设置背景渐变
    private void set_background(){
        // 定义自定义颜色值
        int startColor = Color.parseColor("#FFFFEEA2"); // FFF8DC5E
        int endColor = Color.parseColor("#FFF8DC5E"); // FFFFEEA2

// 创建渐变色背景
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {startColor, endColor}
        );

// 设置背景形状（此处为矩形）
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

// 应用渐变色背景到 View
        View view = findViewById(R.id.person_bj_jianbian);
        view.setBackground(gradientDrawable);


    }
}