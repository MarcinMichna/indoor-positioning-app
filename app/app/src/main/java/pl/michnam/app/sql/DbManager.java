package pl.michnam.app.sql;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.view.AreaItemList;
import pl.michnam.app.sql.entity.AreaData;
import pl.michnam.app.util.Tag;

public class DbManager extends SQLiteOpenHelper {
    private final String AREA = "area";
    private final String AREA_ID = "id";
    private final String AREA_AREA_NAME = "area_name";
    private final String AREA_NAME = "name";
    private final String AREA_ADDR = "address";
    private final String AREA_TYPE = "type";
    private final String AREA_MIN_RSSI = "min_rssi";
    private final String AREA_MAX_RSSI = "max_rssi";


    public DbManager(Context context) {
        super(context, AppConfig.databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS area (" +
                        "id INTEGER primary key autoincrement, " +
                        "area_name TEXT," +
                        "name TEXT," +
                        "address TEXT," +
                        "type TEXT," +
                        "min_rssi INTEGER, " +
                        "max_rssi INTEGER);"

        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS area");
        onCreate(db);
    }

    public void addNewArea(ArrayList<AreaItemList> data, String areaName) {
        Log.i(Tag.DB, "Adding " + data.size() + " items to db");
        SQLiteDatabase db = this.getWritableDatabase();
        for (AreaItemList item : data) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AREA_AREA_NAME, areaName);
            contentValues.put(AREA_NAME, item.getName());
            contentValues.put(AREA_ADDR, item.getAddress());
            contentValues.put(AREA_TYPE, "wifi"); // TODO add BT support
            contentValues.put(AREA_MIN_RSSI, item.getMinRssi());
            contentValues.put(AREA_MAX_RSSI, item.getMaxRssi());
            db.insert(AREA, null, contentValues);
        }
    }

    public ArrayList<AreaData> getAreaData(String areaName) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<AreaData> results = new ArrayList<>();

        String query = "select * from " + AREA + " where " + AREA_AREA_NAME + " = '" + areaName + "'";
        @SuppressLint("Recycle") Cursor res = db.rawQuery(query, null);
        res.moveToFirst();

        int id;
        String name;
        String address;
        String type;
        int minRssi;
        int maxRssi;

        while (!res.isAfterLast()) {
            id = res.getInt(res.getColumnIndex(AREA_ID));
            name = res.getString(res.getColumnIndex(AREA_NAME));
            address = res.getString(res.getColumnIndex(AREA_ADDR));
            type = res.getString(res.getColumnIndex(AREA_TYPE));
            minRssi = res.getInt(res.getColumnIndex(AREA_MIN_RSSI));
            maxRssi = res.getInt(res.getColumnIndex(AREA_MAX_RSSI));

            results.add(new AreaData(id, name, address, type, minRssi, maxRssi, areaName));
            res.moveToNext();
        }

        return results;
    }

    public ArrayList<String> getAreasList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> results = new ArrayList<>();

        String query = "SELECT DISTINCT " + AREA_AREA_NAME + " FROM " + AREA;
        @SuppressLint("Recycle") Cursor res = db.rawQuery(query, null);
        res.moveToFirst();


        while (!res.isAfterLast()) {
            results.add(res.getString(res.getColumnIndex(AREA_AREA_NAME)));
            res.moveToNext();
        }

        return results;
    }

    public HashMap<String, ArrayList<AreaData>> getAllAreasInfo() {
        HashMap<String, ArrayList<AreaData>> res = new HashMap<>();

        for (String area : getAreasList()) {
            res.put(area, getAreaData(area));
        }

        return res;
    }


}
