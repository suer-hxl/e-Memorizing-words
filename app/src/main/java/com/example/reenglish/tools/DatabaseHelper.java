package com.example.reenglish.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String COLUMN_ID ="id" ;
    private static final String DATABASE_NAME = "reenglishv1.db";
    private static final int DATABASE_VERSION = 306;
    public static final String wordbook = "wordbook";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 在这里执行数据库表的创建操作（如果需要的话）
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在这里执行数据库升级操作（如果需要的话）
    }

}
