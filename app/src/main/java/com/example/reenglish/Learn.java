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
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Learn extends AppCompatActivity {

    TextToSpeech tts;
    private DatabaseHelper databaseHelper;

    private static List<String> wordListA; // 存储组A的单词列表
    private static List<String> wordListB; // 存储组B的单词列表
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

        super.onCreate(savedInstanceState);
        //闪屏设置为空
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_leran);

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



        gaosi();//设置高斯模糊


        databaseHelper = new DatabaseHelper(this);

        wordListB=  new ArrayList<>();// 存储组B的单词列表
        wordListA = getWordList(); // 获取一组单词数据

        lista=wordListA.size();
        Log.d("获取a表大小", "onCreate: "+lista);

        //user表获取wordid,如果有代表收藏,展示黄星
        if (get_start(get_id())){
            ImageView  start=findViewById(R.id.l_start);
            start.setImageResource(R.drawable.ic_baseline_star_24);
        }


        String firstWord = wordListA.get(currentIndex);
        String[] fields = firstWord.split(" - ");
        //分割字符,显示首个 单词和音标
        String english = fields[1];
        String phoneticUk = fields[int_pr];
        TextView wordTextEn = findViewById(R.id.learn_show_word);
        wordTextEn.setText(english);
        TextView wordTextPn = findViewById(R.id.learn_show_word_sign);
        wordTextPn.setText(phoneticUk);



//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
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
                        Log.d("TAG", "onInit: 使用us");
                    } else if (country.equals("uk")) {
                        // 使用英式英语
                        locale = Locale.UK;
                        Log.d("TAG", "onInit: 使用uk");

                    } else {
                        // 默认使用英语
                        locale = Locale.ENGLISH;
                    }

                    result = tts.setLanguage(locale);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                        Toast.makeText(Learn.this, "Language not supported!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    //获取10条未学习的数据并将数据作为列表返回
    public List<String> getWordList() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String COLUMN="id,English,Chinese,phonetic_uk,phonetic_us";
        String lim=fr_state;

        String query = "SELECT "+ COLUMN +" FROM wordbook WHERE state = 0 ORDER BY id ASC LIMIT "+lim ;
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

    //隐藏认识和不认识按钮
    public void renshu_burenshi_next(){
        //隐藏认识和不认识按钮
        View wordText1 = findViewById(R.id.learn_renshi);
        wordText1.setVisibility(View.GONE);
        View wordText2 = findViewById(R.id.learn_burenshi);
        wordText2.setVisibility(View.GONE);
        //显示next按钮
        View wordText = findViewById(R.id.learn_fuxi_next);
        wordText.setVisibility(View.VISIBLE);


        //获取当前索引的值
        String firstWord = wordListA.get(currentIndex);

        String[] fields = firstWord.split(" - ");
        //分割字符
        Log.d("fields", "分割字符fields: "+fields);
        String chinese = fields[2];
        //获取中文解释并展示
        TextView wordTextCn = findViewById(R.id.learn_word_text);
        wordTextCn.setVisibility(View.VISIBLE);
        wordTextCn.setText(chinese);
    }

    //不认识的逻辑
    public void learn_fuxi_burenshi(View view) {
        // 延迟执行代码
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                renshu_burenshi_next(); // 在延迟后执行的代码
            }
        }, 200); // 0.5秒的延迟，单位为毫秒
    }

    //记录认识的单词并添加到数组b,修改数据库数据
    public void learn_fuxi_renshi(View view) {
        if (currentIndex==0)count--;//自减放在next时多统计一遍

        if (wordListB.size() != lista) {//如果b表和a表数量一致表示学完

            String word = wordListA.get(currentIndex);//获取当前索引数据
            String[] parts = word.split("-");
            String id = parts[0].trim(); // 获取分割后的第一个部分并去除首尾空格

            update_state(id);//将对应id的单词状态改成1表示已学习
            insert_daily(id);//将日期和id插入日常表统计每天学的单词
            update_review();//插入review表数据
            wordListB.add(id);//添加id
                //如果索引等于0执行代码后将自减索引以免越界和漏查
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renshu_burenshi_next();
                        wordListA.remove(currentIndex);// 在延迟后执行的代码
                        currentIndex--;
                    }
                }, 200); // 0.5秒的延迟，单位为毫秒
        }
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    renshu_burenshi_next();
                    wordListA.remove(currentIndex);// 在延迟后执行的代码
                }
            }, 200); // 0.5秒的延迟，单位为毫秒
        }

    }

    private void insert_daily(String id) {

        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String sql = "INSERT INTO daily (everyday,word_id) VALUES ("+getCurrentDate()+","+id+")";


        db.execSQL(sql);

        // 关闭数据库连接
        db.close();


    }

    //NEXT的处理逻辑
    public void learn_Nxet(View view) {


        ImageView start = findViewById(R.id.l_delete);
        start.setImageResource(R.drawable.ic_baseline_delete_start_24);


        //如果b和a表数量一致表示学完
        if (wordListB.size()==lista){

            //跳转活动:
            Intent intent = new Intent(this, zjy.class);
            intent.putExtra("key", "L");
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
                String phoneticUs = fields[4];
                Log.d("TAG", "learn_Nxet: "+phoneticUs);
                TextView wordTextEn = findViewById(R.id.learn_show_word);
                wordTextEn.setText(english);
                TextView wordTextPn = findViewById(R.id.learn_show_word_sign);
                wordTextPn.setText(phoneticUk);


                String id = fields[0].trim(); // 获取分割后的第一个部分并去除首尾空格
                //将收藏和删除重置
                if (get_start(id))
                {

                    ImageView start1 = findViewById(R.id.l_start);
                    start1.setImageResource(R.drawable.ic_baseline_star_24);
                }
                else {
                    ImageView start1 = findViewById(R.id.l_start);
                    start1.setImageResource(R.drawable.ic_baseline_star_border_24);
                }
            }
            else sb();

            //隐藏中文和next按钮,显示认识和不认识按钮
            TextView wordTextCn = findViewById(R.id.learn_word_text);
            wordTextCn.setVisibility(View.GONE);
            View wordText2 = findViewById(R.id.learn_fuxi_next);
            wordText2.setVisibility(View.GONE);
            View wordText1 = findViewById(R.id.learn_renshi);
            wordText1.setVisibility(View.VISIBLE);
            View wordText23 = findViewById(R.id.learn_burenshi);
            wordText23.setVisibility(View.VISIBLE);

            Log.d("Nxet", "判断索引和组大小: "+currentIndex+","+wordListA.size());

        }

    }
    //展示下一个音标和单词,索引+1
    private void sb() {
        String word = wordListA.get(currentIndex+1);

        String[] parts = word.split("-");
        String id = parts[0].trim(); // 获取分割后的第一个部分并去除首尾空格
        Log.d("sbid", "sb: "+id);

        //将收藏和删除重置
        if (get_start(id))
        {

            ImageView start1 = findViewById(R.id.l_start);
            start1.setImageResource(R.drawable.ic_baseline_star_24);
        }
        else {
            ImageView start1 = findViewById(R.id.l_start);
            start1.setImageResource(R.drawable.ic_baseline_star_border_24);
        }
        String[] fields = word.split(" - ");
        String english = fields[1];
        String phoneticUk = fields[int_pr];
        TextView wordTextPn = findViewById(R.id.learn_show_word_sign);
        TextView wordTextCn = findViewById(R.id.learn_show_word);
        wordTextCn.setText(english);
        wordTextPn.setText(phoneticUk);
        ++currentIndex;
    }

    //更新user表单词状态
    private void update_state(String id){
        // 获取数据库实例
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // 定义更新语句
        String updateQuery = "UPDATE wordbook SET state = 1 WHERE id = ?";

        // 执行更新操作
        db.execSQL(updateQuery, new String[]{id});

        // 关闭数据库连接
        db.close();
    }

    //更新review表状态
    private void update_review(){
        String word_id=get_id();
        String learn_d=getCurrentDate();
        String one= addDaysToDate(learn_d,1);
        String two= addDaysToDate(learn_d,2);
        String three= addDaysToDate(learn_d,6);
        String four= addDaysToDate(learn_d,12);
        String five= addDaysToDate(learn_d,28);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("word_id", word_id);
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
        db.insert("review", null, values);


        db.close();

    }

    //读单词
    public void readWord(String word) {
        // 检查TextToSpeech引擎是否初始化成功
        if (tts != null && !tts.isSpeaking()) {
            // 播放单词
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    //设置高斯模糊
    private void gaosi() {
        //设置背景
        //设置高斯模糊
        ImageView imgBg = findViewById(R.id.learn_imageView);
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

    public void learn_pronounce(View view) {
        String word = wordListA.get(currentIndex);
        // 使用第一个单词进行你的操作
        String[] fields = word.split(" - ");
        //分割字符

        String english = fields[1];
        readWord(english);
    }


    // 将指定日期加上指定天数，并返回结果   比如20230531 30天 后是20230630
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
    // 获取当前日期，并按照指定格式转换为字符串,比如20230531
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return sdf.format(new Date());}
    //获取当前展示的单词id
    private String get_id(){

        String word = wordListA.get(currentIndex);//获取当前索引数据
        String[] parts = word.split("-");
        String id = parts[0].trim(); // 获取分割后的第一个部分并去除首尾空格
        Log.d("get_id  ","get_id: "+id+"word:"+word);
    return  id;
    }

    public void learn_back(View view) {
            finish();
    }

    //点击后变色,同时插入到收藏表
    @SuppressLint("ResourceAsColor")
    public void l_start(View view) {
        //需要先判断是否有数据,没有则执行
        ImageView start111 = findViewById(R.id.l_delete);
        start111.setImageResource(R.drawable.ic_baseline_delete_start_24);
        if (!get_start(get_id()))
        {
        ImageView  start=findViewById(R.id.l_start);
        start.setImageResource(R.drawable.ic_baseline_star_24);


        SQLiteDatabase db = databaseHelper.getWritableDatabase();


        ContentValues values = new ContentValues();

        String id = get_id();
        values.put("start",id);
        db.insert("user", null, values);
        db.close();

        }else Toast.makeText(this, "不要重复收藏!", Toast.LENGTH_SHORT).show();


    }
    //点击变色从收藏表删除,需要注意空值
    public void l_delete(View view) {

        //需要先判断是否有数据
        if (get_start(get_id()))
        {
            ImageView  start=findViewById(R.id.l_delete);
            start.setImageResource(R.drawable.ic_baseline_delete_start_24_g);

            String id = get_id();

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            String deleteQuery = "DELETE from user  WHERE start = ?";
            db.execSQL(deleteQuery, new String[]{id});

            // 关闭数据库连接
            db.close();
            ImageView  start1=findViewById(R.id.l_start);
            start1.setImageResource(R.drawable.ic_baseline_star_border_24);
        }
        else Toast.makeText(this, "没有这个数据无法删除哦!", Toast.LENGTH_SHORT).show();
    }

    //暂时没想好
    public void l_set(View view) {
        ImageView  start=findViewById(R.id.l_delete);
        start.setImageResource(R.drawable.ic_baseline_delete_start_24_g);
    }

    //通过当前单词id查询uesr表start字段, 有id返回ture 没有返回false,
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


}