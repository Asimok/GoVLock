package com.example.gov_lock.facelogin.HttpSerever;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BRDBHelper extends SQLiteOpenHelper {

    public BRDBHelper(Context context) {
        super(context, "brinfo.db", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//brinfo 楼号 房间号
        //db.execSQL("create table brinfo(UUID varchar(60) primary key, BuildingNumber varchar(30),RoomNumber varchar(30)) ");
        db.execSQL("create table brinfo(  BuildingNumber varchar(30) primary key,RoomNumber varchar(30)) ");
        System.out.println("第一次");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        System.out.println("第二次");
    }


}
