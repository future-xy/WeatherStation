package com.sysu.sdcs.weatherstation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBOpenHandler extends SQLiteOpenHelper {
    int version;

    public DBOpenHandler(Context context, String name, SQLiteDatabase.CursorFactory factory,
                         int version) {
        super(context, name, factory, version);
        this.version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 当数据库创建时就用SQL命令创建一个表
        db.execSQL("CREATE TABLE WeatherNow(LocationID text primary key, City text,"
                + "Temperature interger, Icon text, Text text, WindDir text, "
                + "WindScale text, Humidity interger, Precip Int, Vis Int, AirBean text, DayBean text, modified_time timestamp)");
        db.execSQL("CREATE TABLE WeatherDaily(LocationID text primary key, Status text,"
                + "modified_time timestamp)");
        db.execSQL("CREATE TABLE UserInfo(Name text primary key, Password text, Picture blob, "
                + "modified_time timestamp)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
