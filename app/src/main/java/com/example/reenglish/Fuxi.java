package com.example.reenglish;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.reenglish.tools.DatabaseHelper;
import com.example.reenglish.tools.FastBlur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class Fuxi extends AppCompatActivity {

    TextToSpeech tts;

    private DatabaseHelper databaseHelper;

    private List<String> wordListA; // 存储组A的单词列表
    private  List<String> wordListB; // 存储组B的单词列表
    private  List<String> wordListC; // 储存复习表的id
    private  List<String> wordListB1; // 不认识
    private int currentIndex=0; // 当前显示单词的索引
    int count=0;//完整循环次数
    private int lista;
    private static String pr_state;
    private static String fr_state;
    private static String res;
    
    int int_pr=3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏标题栏
        if (getSupportActionBar() != null) { getSupportActionBar().hide();  }
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //创建视图

        super.onCreate(savedInstanceState);

        //闪屏设置为空
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_fuxi);

        Intent intent = getIntent();
        pr_state = intent.getStringExtra("pr_state");
        fr_state = intent.getStringExtra("fr_state");
        res = intent.getStringExtra("back_id");

        if (pr_state.equals("us"))
        {
            TextView pr=findViewById(R.id.learn_pr_state);
            pr.setText("美音:");
            int_pr=4;
        }

        //高斯模糊
        set_gaosi();
        databaseHelper = new DatabaseHelper(this);
        wordListB=  new ArrayList<>();// 存储组B的单词列表
        wordListB1=  new ArrayList<>();// 存储a表单词的id
        wordListA=  new ArrayList<>();// 存单词信息
//-------------------------------------------------------------------------------
        wordListC=getReview();

        wordListA = get_word(); // 获取10组单词数据并赋值到列表A
        lista=wordListA.size();
        Log.d("TAG", "wordListC: "+wordListC+"lista:"+lista);

        Log.d("TAG", "onCreate: "+wordListA);

        //user表获取wordid,如果有代表收藏,展示黄星
        if (get_start(get_id())){
            ImageView  start=findViewById(R.id.f_start);
            start.setImageResource(R.drawable.ic_baseline_star_24);
        }



        String firstWord = wordListA.get(currentIndex);//获取第一组数据
        String[] fields = firstWord.split(" - ");//分割成
        Log.d("fields", "fields: "+ Arrays.toString(fields));
        // [1, A.D., abbr., 公元；“Anno Domini”的缩写, [ˌeɪˈdi:]]

        String english = fields[1];//取英文
        String phoneticUk = fields[int_pr];//取音标
        //展示音标和英文
        TextView wordTextEn = findViewById(R.id.show_word);
        wordTextEn.setText(english);
        TextView wordTextPn = findViewById(R.id.show_word_sign);
        wordTextPn.setText(phoneticUk);




//-------------------------------------------------------------------------
//语音
//---------------------------------------------------
        choose_speech(pr_state);




    }
    //根据不同的国家给出不同的发音比如,us或者uk
    private void choose_speech(String country) {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result;
                    Locale locale;

                    if (country.equals("us")) {
                        // 使用美式英语
                        locale = Locale.US;
                    } else if (country.equals("uk")) {
                        // 使用英式英语
                        locale = Locale.UK;
                    } else {
                        // 默认使用英语
                        locale = Locale.ENGLISH;
                    }

                    result = tts.setLanguage(locale);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                        Toast.makeText(Fuxi.this, "Language not supported!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }





    //查询review表复习日小于当天的所有数据
    private List<String> getReview() {
        String COLUMN = "word_id";
        String currentDate = getCurrentDate();
        List<String> wordIdList = new ArrayList<>();

        // 查询1至5次复习日小于当前时间且state=0的数据
        for (int i = 1; i <= 5; i++) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();

            String query = "SELECT " + COLUMN + " FROM review WHERE " +
                    getColumnName(i) + " <= " + currentDate + " AND " + getStateColumnName(i) + " = 0 ORDER BY word_id ASC LIMIT 100";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String wordId = cursor.getString(cursor.getColumnIndex("word_id"));
                    if (wordIdList.contains(wordId)) {
                        update_review(wordId);  // 更新对应id状态
                        Log.d("cursor:"+i, "getReview: 重复"+wordId+"更新全部状态初始日期为"+currentDate);
                    } else {
                        wordIdList.add(wordId);
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();

        }


        return wordIdList;
    }

    private String getColumnName(int index) {
        switch (index) {
            case 1:
                return "one";
            case 2:
                return "two";
            case 3:
                return "three";
            case 4:
                return "four";
            case 5:
                return "five";
            default:
                throw new IllegalArgumentException("Invalid index: " + index);
        }
    }

    private String getStateColumnName(int index) {
        switch (index) {
            case 1:
                return "one_state";
            case 2:
                return "two_state";
            case 3:
                return "three_state";
            case 4:
                return "four_state";
            case 5:
                return "five_state";
            default:
                throw new IllegalArgumentException("Invalid index: " + index);
        }
    }




    //更新review表状态
    private void update_review(String id){

        String learn_d=getCurrentDate();
        String one= addDaysToDate(learn_d,1);
        String two= addDaysToDate(learn_d,2);
        String three= addDaysToDate(learn_d,6);
        String four= addDaysToDate(learn_d,12);
        String five= addDaysToDate(learn_d,28);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("learn_date", learn_d);
        values.put("one", one);
        values.put("two", two);
        values.put("three", three);
        values.put("four", four);
        values.put("five", five);
        values.put("one_state", 0);
        values.put("two_state", 0);
        values.put("three_state", 0);
        values.put("four_state", 0);
        values.put("five_state", 0);
        String whereClause = "word_id="+id; // 将 "condition" 替换为实际的 WHERE 子句

        db.update("review", values, whereClause, null);

        db.close();

    }


    //通过word_id获取单词信息
    private List<String> getWordList(String word_id){
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String COLUMN="id,English,Chinese,phonetic_uk,phonetic_us";

        String query = "SELECT "+ COLUMN +" FROM wordbook WHERE id="+word_id ;
        Cursor cursor = db.rawQuery(query, null);
        List<String> wordList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // 获取每条数据的具体字段值
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String english = cursor.getString(cursor.getColumnIndex("English"));
                @SuppressLint("Range") String chinese = cursor.getString(cursor.getColumnIndex("Chinese"));
                @SuppressLint("Range") String phonericUk = cursor.getString(cursor.getColumnIndex("phonetic_uk"));
                @SuppressLint("Range") String phonericUs = cursor.getString(cursor.getColumnIndex("phonetic_us"));

                // 将字段值组合成一条数据，并添加到列表中
                String word = id+" - "+english + " - " + chinese + " - " + phonericUk + " - " + phonericUs;
                wordList.add(word);

            } while (cursor.moveToNext());


        }

        cursor.close();
        Log.d("TAG", "getWordList: "+wordList);
        return wordList;

    }

    //获取C表存储的id,用于取数据.
    private List<String> get_word(){
        List<String> wordListD = new ArrayList<>() ;
        int N= Integer.parseInt(fr_state);


        if (wordListC.size() > N)
        {for (int i = 0; i < N; i++) {
            String element = wordListC.get(i);  // 获取元素
            wordListD.add(String.valueOf(getWordList(element)));

        }
        }

        if (wordListC.size() <= N) {
            for (int i = 0; i< wordListC.size(); i++) {
                String element = wordListC.get(i);  // 获取元素
                wordListD.add(String.valueOf(getWordList(element)));
            }
        }
        Log.d("wordListD", "get_word: "+wordListD);
        Log.d("wordListD", "get_word: "+wordListD);


        return   wordListD;
    }

    //获取10条未学习的数据并将数据作为列表返回---废弃方法
    private List<String> getWordList() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String COLUMN="id,English,Chinese,phonetic_uk,phonetic_us";

        String query = "SELECT "+ COLUMN +" FROM wordbook WHERE state = 1 ORDER BY id ASC LIMIT 10" ;
        Cursor cursor = db.rawQuery(query, null);
        List<String> wordList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // 获取每条数据的具体字段值
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String english = cursor.getString(cursor.getColumnIndex("English"));
                @SuppressLint("Range") String chinese = cursor.getString(cursor.getColumnIndex("Chinese"));
                @SuppressLint("Range") String phonericUk = cursor.getString(cursor.getColumnIndex("phonetic_uk"));
                @SuppressLint("Range") String phonericUs = cursor.getString(cursor.getColumnIndex("phonetic_us"));

                // 将字段值组合成一条数据，并添加到列表中
                String word = id+" - "+english + " - " + chinese + " - " + phonericUk + " - " + phonericUs;
                wordList.add(word);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return wordList;
    }

    // 展示next控件,隐藏认识和不认识
    private void renshu_burenshi_next(){
        //隐藏认识和不认识按钮
        View wordText1 = findViewById(R.id.renshi);
        wordText1.setVisibility(View.GONE);
        View wordText2 = findViewById(R.id.burenshi);
        wordText2.setVisibility(View.GONE);
        //显示next按钮
        View wordText = findViewById(R.id.fuxi_next);
        wordText.setVisibility(View.VISIBLE);


        //获取当前索引的值,获取中文解释并赋值给word_text展示
        String firstWord = wordListA.get(currentIndex);
        Log.d("fuxi_next," ,"获取当前索引的值: "+currentIndex);

        String[] fields = firstWord.split(" - ");//分割字符
        String chinese = fields[2];//获取中文解释并赋值给word_text展示
        TextView wordTextCn = findViewById(R.id.word_text);
        wordTextCn.setVisibility(View.VISIBLE);
        wordTextCn.setText(chinese);


    }
    //记录认识的单词并添加到数组b
    public void fuxi_renshi(View view) {
        if (currentIndex==0)count--;//自减放在next时多统计一遍

        if (wordListB.size() != lista) {//这里的10要修改成初始的a表大小
            String id = get_id();
            wordListB.add(id);//添加id
            boolean containsValue = wordListB1.contains(id);

            if (containsValue) {
                update_review(id);
                Log.d("wordListB1", "wordListB1: "+wordListB1);
            } else {
                getReviewStates(id);
            }



            Log.d("wordListB", "wordListB添加id: "+wordListB);

            if (currentIndex==0) {
                //如果索引等于0执行代码后将自减索引以免越界和漏查
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renshu_burenshi_next();
                        wordListA.remove(currentIndex);// 在延迟后执行的代码
                        currentIndex=-1;
                    }
                }, 200); // 0.5秒的延迟，单位为毫秒


                Log.d("定位", "fuxi_renshi: ");


            }else {

                // 延迟执行代码
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renshu_burenshi_next(); // 在延迟后执行的代码
                        wordListA.remove(currentIndex);
                        --currentIndex;
                    }
                }, 200); // 0.5秒的延迟，单位为毫秒
            }
        }





    }
    //不认识的逻辑,把id加到B1表然后一直显示第一个
    public void fuxi_burenshi(View view) {
        String word = wordListA.get(currentIndex);//获取当前索引数据
        String number = word.substring(1, word.indexOf(" -"));
        wordListB1.add(number);
        //把id加到B1表
        Log.d("number", "fuxi_burenshi: "+number);
        // 延迟执行代码
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                renshu_burenshi_next(); // 在延迟后执行的代码
            }
        }, 200); // 0.5秒的延迟，单位为毫秒
    }


    //NEXT的处理逻辑
    public void Nxet(View view) {

        ImageView start = findViewById(R.id.f_delete);
        start.setImageResource(R.drawable.ic_baseline_delete_start_24);
        ImageView start121212 = findViewById(R.id.f_start);
        start121212.setImageResource(R.drawable.ic_baseline_star_border_24);
        //b表=10,表示十个单词背完结束关闭页面
        if (wordListB.size() == lista){
            //这里的10要修改成初始的a表大小
            //跳转活动:
            Intent intent = new Intent(this, zjy.class);
            intent.putExtra("key", "");
            String r1e= String.valueOf(res);
            intent.putExtra("back_id", r1e);
            intent.putExtra("fr_state", fr_state);
            intent.putExtra("pr_state", pr_state);
            finish();

            // 启动目标活动
            startActivity(intent);
        }

        //如果当前索引=表的数量大小表示当前表被学习过一遍.重置索引为0,然后展示第一个单词和音标
        if (!wordListA.isEmpty()){
            if (currentIndex==wordListA.size()-1)//如果当前索引=表的数量大小表示当前表被学习过一遍.重置索引为0
            {
                currentIndex=0;
                //展示第一个单词和音标
                String firstWord = wordListA.get(currentIndex);
                String[] fields = firstWord.split(" - ");
                //分割字符
                String english = fields[1];
                String phoneticUk = fields[int_pr];
                TextView wordTextEn = findViewById(R.id.show_word);
                wordTextEn.setText(english);
                TextView wordTextPn = findViewById(R.id.show_word_sign);
                wordTextPn.setText(phoneticUk);
                if (get_start(get_id())){
                    ImageView start1 = findViewById(R.id.f_start);
                    start1.setImageResource(R.drawable.ic_baseline_star_24);
                }

            }
            else sb();

            //移除中文解释,移除next按钮,展示认识和不认识
            TextView wordTextCn = findViewById(R.id.word_text);
            wordTextCn.setVisibility(View.GONE);
            View wordText2 = findViewById(R.id.fuxi_next);
            wordText2.setVisibility(View.GONE);
            View wordText1 = findViewById(R.id.renshi);
            wordText1.setVisibility(View.VISIBLE);
            View wordText23 = findViewById(R.id.burenshi);
            wordText23.setVisibility(View.VISIBLE);

        }


    }
    //展示下一个音标和单词,隐藏中文
    public void sb(){
        String word = wordListA.get(currentIndex+1);
        String id = word.substring(1, word.indexOf(" -"));
        Log.d("sbid", "sb: "+id);
        if (get_start(id))
        {
            ImageView start1 = findViewById(R.id.f_start);
            start1.setImageResource(R.drawable.ic_baseline_star_24);
        } else {
            ImageView start1 = findViewById(R.id.f_start);
            start1.setImageResource(R.drawable.ic_baseline_star_border_24);
        }
            //分割字符
        String[] fields = word.split(" - ");
            String english = fields[1];
            String phoneticUk = fields[int_pr];
            //展示下一个音标和单词
            TextView wordTextEn = findViewById(R.id.show_word);
            wordTextEn.setText(english);
            TextView wordTextPn = findViewById(R.id.show_word_sign);
            wordTextPn.setText(phoneticUk);
            ++currentIndex;

    }



    private void getReviewStates(String wordId){
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String query = "SELECT one_state, two_state, three_state, four_state, five_state FROM review WHERE word_id = ?";
            String[] selectionArgs = {wordId};
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String oneState = cursor.getString(cursor.getColumnIndex("one_state"));
                @SuppressLint("Range") String twoState = cursor.getString(cursor.getColumnIndex("two_state"));
                @SuppressLint("Range") String threeState = cursor.getString(cursor.getColumnIndex("three_state"));
                @SuppressLint("Range") String fourState = cursor.getString(cursor.getColumnIndex("four_state"));
                @SuppressLint("Range") String fiveState = cursor.getString(cursor.getColumnIndex("five_state"));

                // 判断字段的状态,再修改  如果字段=0则改1
                // 判断字段状态并更新
                if (oneState.equals("0")) {
                    updateReviewState(db, wordId, "one_state");
                } else if (twoState.equals("0")) {
                    updateReviewState(db, wordId, "two_state");
                } else if (threeState.equals("0")) {
                    updateReviewState(db, wordId, "three_state");
                } else if (Objects.equals(fourState, "0")) {
                    updateReviewState(db, wordId, "four_state");
                } else if (Objects.equals(fiveState, "0")) {
                    updateReviewState(db, wordId, "five_state");
                }



            }
            cursor.close();
            db.close();
    }
    //更新数据库
    private void updateReviewState(SQLiteDatabase db, String wordId, String fieldName) {
        ContentValues values = new ContentValues();
        values.put(fieldName, 1);

        String whereClause = "word_id = ?";
        String[] whereArgs = {wordId};

        db.update("review", values, whereClause, whereArgs);
    }

    private String getCurrentDate() {
        // 获取当前日期，并按照指定格式转换为字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String addDaysToDate(String date, int days) {
        // 将指定日期加上指定天数，并返回结果
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        try {
            Date currentDate = null;
            try {
                currentDate = sdf.parse(date);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_YEAR, days);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }



    //设置高斯模糊设置背景
    public void set_gaosi(){

        //设置背景
        //设置高斯模糊
        ImageView imgBg = findViewById(R.id.fuxi_imageView);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), Integer.parseInt(res));
        int scaleRatio = 20;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mBitmap,
                mBitmap.getWidth() / scaleRatio,
                mBitmap.getHeight() / scaleRatio,
                false);
        int blurRadius = 30;
        Bitmap blurBitmap = FastBlur.doBlur(scaledBitmap, blurRadius, true);
        imgBg.setImageBitmap(blurBitmap);

    }

    //读单词
    public void readWord(String word) {
        // 检查TextToSpeech引擎是否初始化成功
        if (tts != null && !tts.isSpeaking()) {
            // 播放单词
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    //点击背景调用读单词
    public void pronounce(View view) {

        String word = wordListA.get(currentIndex);
        // 使用第一个单词进行你的操作
        String[] fields = word.split(" - ");
        //分割字符

        String english = fields[1];
        readWord(english);
    }


    public void fuxi_back(View view) {
        finish();
    }

    public void f_strat(View view) {
        ImageView start111 = findViewById(R.id.f_delete);
        start111.setImageResource(R.drawable.ic_baseline_delete_start_24);

        //需要先判断是否有数据
        if (!get_start(get_id()))
        {
        ImageView  start=findViewById(R.id.f_start);
        start.setImageResource(R.drawable.ic_baseline_star_24);
            SQLiteDatabase db = databaseHelper.getWritableDatabase();


            ContentValues values = new ContentValues();

            String id = get_id();
            values.put("start",id);
            db.insert("user", null, values);
            db.close();

        }else Toast.makeText(this, "不要重复收藏!", Toast.LENGTH_SHORT).show();


    }


    public void f_set(View view) {


       }

    public void f_delete(View view) {

        //需要先判断是否有数据
        if (get_start(get_id()))
        {
            ImageView  start=findViewById(R.id.f_delete);
            start.setImageResource(R.drawable.ic_baseline_delete_start_24_g);

            String id = get_id();

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            String deleteQuery = "DELETE from user  WHERE start = ?";
            db.execSQL(deleteQuery, new String[]{id});

            // 关闭数据库连接
            db.close();
            ImageView  start1=findViewById(R.id.f_start);
            start1.setImageResource(R.drawable.ic_baseline_star_border_24);
        }
        else Toast.makeText(this, "没有这个数据无法删除哦!", Toast.LENGTH_SHORT).show();



    }
    //查询uesr表start字段,通过id和word查询
    private boolean get_start(String id) {
        boolean isStart = false;

        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // 定义SQL语句
        String sql = "SELECT start FROM user WHERE start = ?";

        // 执行查询
        Cursor cursor = db.rawQuery(sql, new String[]{id});

        // 判断查询结果
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("start");
            if (cursor.isNull(columnIndex)) {
                isStart = false; // 结果为空
            } else {
                isStart = true; // 结果不为空
            }
        }

        // 关闭游标和数据库连接
        cursor.close();
        db.close();

        return isStart;
    }

    private String get_id(){

        String word = wordListA.get(currentIndex);//获取当前索引数据
        Log.d("TAG", "get_id: "+word);
        String id = word.substring(1, word.indexOf(" -"));
        Log.d("TAG", "get_id: "+id);

        return  id;
    }





}