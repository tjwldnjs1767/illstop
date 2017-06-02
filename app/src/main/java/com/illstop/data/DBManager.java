package com.illstop.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.illstop.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DBManager extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "TourAPI";

    public static final String AREA_CODE_TABLE_NAME = "areacode";

    public FileReader file;

    private BufferedReader buffer;

    private SQLiteDatabase db;

    private Context context;
    public DBManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // Called once when executed
    @Override
    public void onCreate(SQLiteDatabase db) {
        /* Create table for save areㄹa code data */
        InputStream inputStream = context.getResources().openRawResource(R.raw.area_codes);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        buffer = new BufferedReader(inputStreamReader);
        String CREATE_TABLE_SQL = "CREATE TABLE " + AREA_CODE_TABLE_NAME + "(areaCode INTEGER, sigunguCode INTEGER, areaName TEXT, sigunguName TEXT);";
        db.execSQL(CREATE_TABLE_SQL);

		/* Insert area code data into areacode table from csv format text file */
        String INSERT_DATA_SQL = "INSERT INTO " + AREA_CODE_TABLE_NAME + " VALUES (";
        String line = "";

        db.beginTransaction();

        try {
            StringBuilder sb = null;
            while ((line = buffer.readLine()) != null) {
                sb = new StringBuilder(INSERT_DATA_SQL);
                String[] str = line.split(",");

                sb.append(str[0] + ",");
                sb.append(str[1] + ",'");
                sb.append(str[2] + "','");
                sb.append(str[3] + "');");

                db.execSQL(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS areacode";
        db.execSQL(sql);
        onCreate(db);

        this.db = db;
    }

    public String[] getCode(String area, String sigungu) {
        Log.i("ddd", area + " " + sigungu);
        String GET_CODE_SQL = "SELECT areaCode, sigunguCode FROM " + AREA_CODE_TABLE_NAME + " WHERE "
                + "areaName = '" + area + "' AND "
                + "sigunguName = '" + sigungu + "';";

        db = getReadableDatabase();
        Cursor c = db.rawQuery(GET_CODE_SQL, null);
        c.moveToNext();

        // There is only one row
        String[] code = new String[2];
        code[0] = "" + c.getInt(0);
        code[1] = "" + c.getInt(1);

        // code[0] is areaCode
        // code[1] is sigunguCode
        return code;
    }
}