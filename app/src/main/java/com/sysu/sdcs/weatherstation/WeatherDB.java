package com.sysu.sdcs.weatherstation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class WeatherDB {
    private DBOpenHandler dbOpenHandler;

    public WeatherDB(Context context) {
        this.dbOpenHandler = new DBOpenHandler(context, "dbWeather.db3", null, 1);
    }

    public Uri insert(Uri uri, String tableName, ContentValues cv) {
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();

        db.insert(tableName, null, cv);
        db.close();
        return uri;
    }

    public int update(Uri uri, String tableName, ContentValues cv, String where, String[] whereArgs) {
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();
        int ret = db.update(tableName, cv, where, whereArgs);
        db.close();
        return ret;
    }

    public int delete(Uri uri, String tableName, String where, String[] whereArgs) {
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();
        int ret = db.delete(tableName, where, whereArgs);
        db.close();
        return ret;
    }

    public Cursor query(Uri uri, String tableName, String[] projection, String where, String[] whereArgs, String sortOrder) {
        SQLiteDatabase db = dbOpenHandler.getWritableDatabase();
        //        db.close();
        return db.query(tableName, projection, where, whereArgs, null, null, sortOrder, null);
    }
    //////
    //还需要定义操作函数
}

