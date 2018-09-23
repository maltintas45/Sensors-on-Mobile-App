package com.maltintas.zer.sm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.maltintas.zer.sm.FeedReaderContract.FeedEntry;
import com.maltintas.zer.sm.FeedReaderContract.AppSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SeMu.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.TIMESTAMP + " TEXT," +
                    FeedEntry.ACCELEROMETER_X + " TEXT," +
                    FeedEntry.ACCELEROMETER_Y + " TEXT," +
                    FeedEntry.ACCELEROMETER_Z + " TEXT," +
                    FeedEntry.GYROSCOPE_X + " TEXT," +
                    FeedEntry.GYROSCOPE_Y + " TEXT," +
                    FeedEntry.GYROSCOPE_Z + " TEXT," +
                    FeedEntry.LIGHT + " TEXT," +
                    FeedEntry.PROXIMITY + " TEXT,"+
                    FeedEntry.GPS_LONGITUDE+" TEXT,"+
                    FeedEntry.GPS_LATITUDE+" TEXT);";

    private static final String SQL_CREATE_SETTINGS =
            "CREATE TABLE " + AppSet.TABLE_NAME + " (" +
                    AppSet._ID + " INTEGER PRIMARY KEY," +
                    AppSet.USER_NICK+" TEXT,"+
                    AppSet.USER_GENDER+" TEXT,"+
                    AppSet.EXPLANATION+" TEXT,"+
                    AppSet.DEVICE_ID+ " TEXT);";
    private static final String SQL_INSERT_SETTINGS =
            "INSERT INTO " + AppSet.TABLE_NAME + " (" +
                    AppSet.USER_NICK+","+
                    AppSet.USER_GENDER+","+
                    AppSet.EXPLANATION+","+
                    AppSet.DEVICE_ID+ ") "+
                    "VALUES ('','','','"+createID(4,4) +"');";
    private static String createID(int id_lenght,int token_size)
    {
        Random r = new Random();
        int Low = 65;
        int High = 90;
        int AsciiValue=65;
        List<String> ID=new ArrayList<String>(  );
        String token="";
        for(int i=0;i<id_lenght;i++) {
            for (int j = 0; j < token_size; j++) {
                AsciiValue = r.nextInt( High - Low ) + Low;
                token += Character.toString( (char) AsciiValue );
            }
            ID.add( token );
            token = "";
        }
        return android.text.TextUtils.join("-",ID);
    }


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    private static final String SQL_DELETE_SETTINGS =
            "DROP TABLE IF EXISTS " + AppSet.TABLE_NAME;

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL( SQL_CREATE_SETTINGS);
        db.execSQL( SQL_INSERT_SETTINGS );
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL( SQL_DELETE_SETTINGS );
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
