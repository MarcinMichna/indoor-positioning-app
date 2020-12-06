package pl.michnam.app.sql;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import pl.michnam.app.R;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.http.model.HotspotResult;
import pl.michnam.app.core.view.AreaItem;
import pl.michnam.app.sql.entity.AreaData;
import pl.michnam.app.sql.entity.HotspotData;
import pl.michnam.app.util.Tag;

public class DbManager extends SQLiteOpenHelper {
    private final String AREA = "area";
    private final String AREA_ID = "id";
    private final String AREA_NAME = "area_name";
    private final String AREA_DEVICE_NAME = "name";
    private final String AREA_ADDR = "address";
    private final String AREA_TYPE = "type";
    private final String AREA_MIN_RSSI = "min_rssi";
    private final String AREA_MAX_RSSI = "max_rssi";
    private final String AREA_AVG = "avg";
    private final String AREA_SD = "sd";


    private final String HOTSPOT = "hotspot";
    private final String HOTSPOT_ID = "id";
    private final String HOTSPOT_AREA_NAME = "area_name";
    private final String HOTSPOT_ESP = "esp";
    private final String HOTSPOT_MIN_RSSI = "min_rssi";
    private final String HOTSPOT_MAX_RSSI = "max_rssi";
    private final String HOTSPOT_AVG = "avg";
    private final String HOTSPOT_SD = "sd";


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
                        "max_rssi INTEGER," +
                        "avg REAL," +
                        "sd REAL);"
        );
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS "+ HOTSPOT +" (" +
                        "id INTEGER primary key autoincrement, " +
                        "area_name TEXT," +
                        "esp TEXT," +
                        "type TEXT," +
                        "min_rssi INTEGER, " +
                        "max_rssi INTEGER," +
                        "avg REAL," +
                        "sd REAL);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS area");
        db.execSQL("DROP TABLE IF EXISTS hotspot");
        onCreate(db);
    }

    public void addNewArea(ArrayList<AreaItem> data, String areaName) {
        Log.i(Tag.DB, "Adding " + data.size() + " items to db");
        SQLiteDatabase db = this.getWritableDatabase();
        for (AreaItem item : data) {
            ContentValues values = new ContentValues();
            values.put(AREA_NAME, areaName);
            values.put(AREA_DEVICE_NAME, item.getName());
            values.put(AREA_ADDR, item.getAddress());
            values.put(AREA_MIN_RSSI, item.getMinRssi());
            values.put(AREA_MAX_RSSI, item.getMaxRssi());
            values.put(AREA_AVG, item.getAvg());
            values.put(AREA_SD, item.getSd());
            if (item.isBt()) values.put(AREA_TYPE, "bt");
            else values.put(AREA_TYPE, "wifi");

            db.insert(AREA, null, values);
        }
    }

    public void addNewAreaHotspot(ArrayList<HotspotData> data, String areaName) {
        Log.i(Tag.DB, "Adding " + data.size() + " items to db - hotspot");
        SQLiteDatabase db = this.getWritableDatabase();
        for (HotspotData item : data) {
            ContentValues values = new ContentValues();
            values.put(HOTSPOT_AREA_NAME, areaName);
            values.put(HOTSPOT_ESP, item.getEsp());
            values.put(HOTSPOT_MIN_RSSI, item.getMinRssi());
            values.put(HOTSPOT_MAX_RSSI, item.getMaxRssi());
            values.put(HOTSPOT_AVG, item.getAvg());
            values.put(HOTSPOT_SD, item.getSd());

            db.insert(HOTSPOT, null, values);
        }
    }

    public ArrayList<AreaData> getAreaData(String areaName) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<AreaData> results = new ArrayList<>();

        String query = "select * from " + AREA + " where " + AREA_NAME + " = '" + areaName + "'";
        @SuppressLint("Recycle") Cursor res = db.rawQuery(query, null);
        res.moveToFirst();

        int id;
        String name;
        String address;
        String type;
        int minRssi;
        int maxRssi;
        float avg;
        float sd;

        while (!res.isAfterLast()) {
            id = res.getInt(res.getColumnIndex(AREA_ID));
            name = res.getString(res.getColumnIndex(AREA_DEVICE_NAME));
            address = res.getString(res.getColumnIndex(AREA_ADDR));
            type = res.getString(res.getColumnIndex(AREA_TYPE));
            minRssi = res.getInt(res.getColumnIndex(AREA_MIN_RSSI));
            maxRssi = res.getInt(res.getColumnIndex(AREA_MAX_RSSI));
            avg = res.getInt(res.getColumnIndex(AREA_AVG));
            sd = res.getInt(res.getColumnIndex(AREA_SD));

            results.add(new AreaData(id, name, address, type, minRssi, maxRssi, areaName, avg, sd));
            res.moveToNext();
        }

        return results;
    }

    public ArrayList<String> getAreasList() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> results = new ArrayList<>();

        String query = "SELECT DISTINCT " + AREA_NAME + " FROM " + AREA;
        @SuppressLint("Recycle") Cursor res = db.rawQuery(query, null);
        res.moveToFirst();


        while (!res.isAfterLast()) {
            results.add(res.getString(res.getColumnIndex(AREA_NAME)));
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

    public void resetAreas(Context context) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "DELETE FROM " + AREA;
        db.execSQL(query);

        Toast.makeText(context, context.getString(R.string.cleared_areas), Toast.LENGTH_LONG).show();
    }

    public void deleteAreas(Context context, ArrayList<String> areas) {
        if (areas.size() > 0) {
            SQLiteDatabase db = this.getReadableDatabase();
            for (String area : areas) {
                String query = "DELETE FROM " + AREA + " WHERE " + AREA_NAME + " = '" + area + "'";
                db.execSQL(query);
            }
            Log.i(Tag.DB, "Deleted areas: " + areas);
        }


    }

    public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS area");
        db.execSQL("DROP TABLE IF EXISTS hotspot");
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS area (" +
                        "id INTEGER primary key autoincrement, " +
                        "area_name TEXT," +
                        "name TEXT," +
                        "address TEXT," +
                        "type TEXT," +
                        "min_rssi INTEGER, " +
                        "max_rssi INTEGER," +
                        "avg REAL," +
                        "sd REAL);"
        );
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS "+ HOTSPOT +" (" +
                        "id INTEGER primary key autoincrement, " +
                        "esp TEXT," +
                        "type TEXT," +
                        "min_rssi INTEGER, " +
                        "max_rssi INTEGER," +
                        "avg REAL," +
                        "sd REAL);"
        );
    }


}
