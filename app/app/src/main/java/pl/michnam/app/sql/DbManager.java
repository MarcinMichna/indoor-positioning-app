package pl.michnam.app.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import pl.michnam.app.config.AppConfig;

public class DbManager extends SQLiteOpenHelper {
    public DbManager(Context context) {
        super(context, AppConfig.databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table wificurrent(" +
                "id INTEGER primary key autoincrement," +
                "ssid TEXT," +
                "rssi INTEGER," +
                "timestamp DATETIME);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS wificurrent");
        onCreate(db);
    }
}
