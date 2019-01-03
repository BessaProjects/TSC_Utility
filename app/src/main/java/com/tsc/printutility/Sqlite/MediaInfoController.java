package com.tsc.printutility.Sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tsc.printutility.Entity.MediaInfo;

import java.util.ArrayList;
import java.util.List;


public class MediaInfoController {
    // 表格名稱
    public static final String TABLE_NAME = "MediaInfo";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_SENSOR_TYPE = "sensorType";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_UPDATE_TIME = "updateTime";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_WIDTH + " DOUBLE NOT NULL, " +
                    COLUMN_HEIGHT + " DOUBLE NOT NULL, " +
                    COLUMN_UNIT + " INTEGER NOT NULL, " +
                    COLUMN_SENSOR_TYPE + " TEXT NOT NULL, " +
                    COLUMN_UPDATE_TIME + " TEXT)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public MediaInfoController(Context context) {
        db = TscSqliteHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public MediaInfo insert(MediaInfo item) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(COLUMN_NAME, item.getName());
        cv.put(COLUMN_WIDTH, item.getWidth());
        cv.put(COLUMN_HEIGHT, item.getHeight());
        cv.put(COLUMN_SENSOR_TYPE, item.getSensorType());
        cv.put(COLUMN_UNIT, item.getUnit());
        cv.put(COLUMN_UPDATE_TIME, item.getUpdateTime());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);
        System.out.println("item:" + id + ", " + item.getName() + ", " + item.getUnit());
        // 回傳結果
        return item;
    }

    // 修改參數指定的物件
    public boolean update(MediaInfo item) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(COLUMN_NAME, item.getName());
        cv.put(COLUMN_WIDTH, item.getWidth());
        cv.put(COLUMN_HEIGHT, item.getHeight());
        cv.put(COLUMN_SENSOR_TYPE, item.getSensorType());
        cv.put(COLUMN_UNIT, item.getUnit());
        cv.put(COLUMN_UPDATE_TIME, item.getUpdateTime());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 讀取所有記事資料
    public List<MediaInfo> getAll() {
        List<MediaInfo> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, COLUMN_UPDATE_TIME + " DESC", null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public MediaInfo get(long id) {
        // 準備回傳結果用的物件
        MediaInfo item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 把Cursor目前的資料包裝為物件
    public MediaInfo getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        MediaInfo result = new MediaInfo();

        result.setId(cursor.getLong(0));
        result.setName(cursor.getString(1));
        result.setWidth(cursor.getDouble(2));
        result.setHeight(cursor.getDouble(3));
        result.setUnit(cursor.getInt(4));
        result.setSensorType(cursor.getString(5));
        result.setUpdateTime(cursor.getString(6));

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

}