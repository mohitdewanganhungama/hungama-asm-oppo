package com.hungama.music.data.database.oldappdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.hungama.music.utils.CommonUtils;

import java.util.Arrays;

public class DataBase {
    private static DataBase db;

    public static synchronized DataBase getInstance(Context context) {
        if (db == null) {
            db = new DataBase(context);
        }
        return db;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_TRACK_CACHE_CREATE);
            db.execSQL(TABLE_ALBUM_CACHE_CREATE);
            db.execSQL(TABLE_PLAYLIST_CACHE_CREATE);
            db.execSQL(TABLE_V_PLAYLIST_CACHE_CREATE);
            db.execSQL(TABLE_VIDEO_ID_LIST_CREATE);
            db.execSQL(TABLE_TRACKLIST_CREATE);
            db.execSQL(TABLE_VIDEO_TRACK_CACHE_CREATE);
            db.execSQL(TABLE_SONGCATCHER_HISTORY_CREATE);
            db.execSQL(TABLE_ALL_STRINGS_CREATE);
            db.execSQL(TABLE_DETAILS_CREATE);
            db.execSQL(TABLE_FAVORITE_LIST_CREATE);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            CommonUtils.INSTANCE.setLog("DbDownVersion", "old:::" + oldVersion + ":::new::::" + newVersion);
            if(newVersion == 7){
                db.execSQL(TABLE_FAVORITE_LIST_CREATE);
            }
            if(newVersion == 6){
                db.execSQL(TABLE_V_PLAYLIST_CACHE_CREATE);
                db.execSQL(TABLE_VIDEO_ID_LIST_CREATE);
            }
            if(newVersion == 5){
                db.execSQL("ALTER TABLE " + Track_Cache_table + " ADD COLUMN " + tables[Track_Cache_int][6] + " TEXT DEFAULT '0'");
                db.execSQL("ALTER TABLE " + Video_Track_Cache_table + " ADD COLUMN " + tables[Video_Track_Cache_int][6] + " TEXT DEFAULT '0'");
            } else {
                checkForTableUpdate(db, Track_Cache_table, Track_Cache_int);
                checkForTableUpdate(db, Video_Track_Cache_table,
                        Video_Track_Cache_int);
                checkForTableUpdate(db, Album_Cache_table, Album_Cache_int);
                checkForTableUpdate(db, Playlist_Cache_table, Playlist_Cache_int);
                checkForTableUpdate(db, Video_Playlist_Cache_table, Video_Playlist_Cache_int);
                checkForTableUpdate(db, Tracklist_table, Tracklist_int);
                checkForTableUpdate(db, VideoIdList_table, VideoIdList_int);
                checkForTableUpdate(db, SongCatcher_History_table,
                        SongCatcher_History_int);
                checkForTableUpdate(db, All_String_Values_table, All_Strings_int);
                checkForTableUpdate(db, Detail_Values_table, Detail_int);
                checkForTableUpdate(db, FavoriteList_table, FavoriteList_int);
                onCreate(db);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                CommonUtils.INSTANCE.setLog("DbVersion","old:::"+oldVersion+":::new::::"+newVersion);
                if(oldVersion < 8) {
                    try {
                        db.execSQL("ALTER TABLE " + Track_Cache_table
                                + " ADD COLUMN " + tables[Track_Cache_int][7]
                                + " TEXT DEFAULT '" + System.currentTimeMillis() + "'");
                    } catch (SQLException e) {
                        if (e.getMessage().contains("no such table")) {
                            db.execSQL(TABLE_TRACK_CACHE_CREATE);
                        }
                        e.printStackTrace();
                    }

                    try {
                        db.execSQL("ALTER TABLE " + Video_Track_Cache_table
                                + " ADD COLUMN " + tables[Video_Track_Cache_int][7]
                                + " TEXT DEFAULT '" + System.currentTimeMillis() + "'");
                    } catch (SQLException e) {
                        if (e.getMessage().contains("no such table")) {
                            db.execSQL(TABLE_VIDEO_TRACK_CACHE_CREATE);
                        }
                        e.printStackTrace();
                    }
                }

                if(newVersion == 7){
                    db.execSQL(TABLE_FAVORITE_LIST_CREATE);
                }
                if(newVersion == 6){
                    db.execSQL(TABLE_V_PLAYLIST_CACHE_CREATE);
                    db.execSQL(TABLE_VIDEO_ID_LIST_CREATE);
                }

                if(newVersion == 5){
                    db.execSQL("ALTER TABLE " + Track_Cache_table + " ADD COLUMN " + tables[Track_Cache_int][6] + " TEXT DEFAULT '0'");
                    try {
                        db.execSQL("ALTER TABLE " + Video_Track_Cache_table + " ADD COLUMN " + tables[Video_Track_Cache_int][6] + " TEXT DEFAULT '0'");
                    } catch (SQLException e) {
                        if (e.getMessage().contains("no such table")) {
                            db.execSQL(TABLE_VIDEO_TRACK_CACHE_CREATE);
                        }
                        e.printStackTrace();
                    }
                }else {
                    checkForTableUpdate(db, Track_Cache_table, Track_Cache_int);
                    checkForTableUpdate(db, Video_Track_Cache_table,
                            Video_Track_Cache_int);
                    checkForTableUpdate(db, Album_Cache_table, Album_Cache_int);
                    checkForTableUpdate(db, Playlist_Cache_table, Playlist_Cache_int);
                    checkForTableUpdate(db, Video_Playlist_Cache_table, Video_Playlist_Cache_int);
                    checkForTableUpdate(db, Tracklist_table, Tracklist_int);
                    checkForTableUpdate(db, VideoIdList_table, VideoIdList_int);
                    checkForTableUpdate(db, SongCatcher_History_table,
                            SongCatcher_History_int);
                    checkForTableUpdate(db, All_String_Values_table, All_Strings_int);
                    checkForTableUpdate(db, Detail_Values_table, Detail_int);
                    checkForTableUpdate(db, FavoriteList_table, FavoriteList_int);
//			checkForTableUpdate(db, MEDIA_CONSUMPTION_TABLE, MEDIA_CONSUMPTION_INT);
                    onCreate(db);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void checkForTableUpdate(SQLiteDatabase db, String table_name,
                                         int table_no) {
            try {
                Cursor cursor = db.rawQuery("SELECT * from " + table_name
                        + " LIMIT 1", null);
                if (cursor != null) {
                    // for(String name : cursor.getColumnNames())
                    // System.out.println(" ::::::::::::::: 61 " + name);
                    if (!Arrays.equals(cursor.getColumnNames(),
                            tables[table_no])) {
                        db.execSQL("DROP TABLE IF EXISTS " + table_name);
                    }
                    cursor.close();
                } else {
                    db.execSQL("DROP TABLE IF EXISTS " + table_name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public enum CacheState {
        NOT_CACHED, QUEUED, CACHING, CACHED, FAILED, PARTIAL;

        public static final CacheState getCacheStateByName(String name) {
            if(!TextUtils.isEmpty(name)) {
                if (name.equalsIgnoreCase(NOT_CACHED.toString())) {
                    return NOT_CACHED;
                } else if (name.equalsIgnoreCase(QUEUED.toString())) {
                    return QUEUED;
                } else if (name.equalsIgnoreCase(CACHING.toString())) {
                    return CACHING;
                } else if (name.equalsIgnoreCase(CACHED.toString())) {
                    return CACHED;
                } else if (name.equalsIgnoreCase(FAILED.toString())) {
                    return FAILED;
                } else if (name.equalsIgnoreCase(PARTIAL.toString())) {
                    return PARTIAL;
                }
            }
            return NOT_CACHED;
        }
    }

    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqLiteDb;
    public static final String DATABASE_NAME = "hungama_music_db";
    private static final int DATABASE_VERSION = 8;
    public static final String Track_Cache_table = "track_cache_table";
    public static final int Track_Cache_int = 0;
    public static final String Album_Cache_table = "album_cache_table";
    public static final int Album_Cache_int = 1;
    public static final String Playlist_Cache_table = "playlist_cache_table";
    public static final int Playlist_Cache_int = 2;
    public static final String Tracklist_table = "tracklist_table";
    public static final int Tracklist_int = 3;
    public static final String Video_Track_Cache_table = "video_track_cache_table";
    public static final int Video_Track_Cache_int = 4;
    public static final String SongCatcher_History_table = "songcatcher_table";
    public static final int SongCatcher_History_int = 5;
    public static final String All_String_Values_table = "all_string_table";
    public static final int All_Strings_int = 6;
    public static final String Detail_Values_table = "details_table";
    public static final int Detail_int = 7;
    public static final String Video_Playlist_Cache_table = "video_playlist_cache_table";
    public static final int Video_Playlist_Cache_int = 8;
    public static final String VideoIdList_table = "video_id_list_table";
    public static final int VideoIdList_int = 9;
    public static final String FavoriteList_table = "favorite_list_table";
    public static final int FavoriteList_int = 10;

    public static final String[][] tables = new String[][] {
            { "sr_no", "track_id", "file_path", "json_details", "state",
                    "progress", "download_reference", "timestamp" },
            { "sr_no", "album_id", "json_details", "state" },
            { "sr_no", "playlist_id", "json_details", "state" },
            { "sr_no", "track_id", "album_id", "playlist_id" },
            { "sr_no", "track_id", "file_path", "json_details", "state",
                    "progress", "download_reference", "timestamp" },
            { "sr_no", "track_id", "title", "json_details", "timestamp" },
            { "sr_no", "str_name", "str_value", "language" },
            { "sr_no", "id", "mediatype", "response", "isfavorite", "updatetime" },
            { "sr_no", "v_playlist_id", "json_details", "state" },
            { "sr_no", "video_id", "album_id", "playlist_id" },
            { "sr_no", "content_id", "fav_state", "type" }};

    private static final String TABLE_TRACK_CACHE_CREATE = "create table IF NOT EXISTS "
            + Track_Cache_table
            + "("
            + tables[Track_Cache_int][0]
            + " integer primary key autoincrement,"
            + tables[Track_Cache_int][1]
            + " text not null, "
            + tables[Track_Cache_int][2]
            + " text not null, "
            + tables[Track_Cache_int][3]
            + " text not null, "
            + tables[Track_Cache_int][4]
            + " text not null, "
            + tables[Track_Cache_int][5]
            + " integer default 0, "
            + tables[Track_Cache_int][6] + " text default 0, "
            + tables[Track_Cache_int][7] + " text default 0);";

    private static final String TABLE_ALBUM_CACHE_CREATE = "create table IF NOT EXISTS "
            + Album_Cache_table
            + "("
            + tables[Album_Cache_int][0]
            + " integer primary key autoincrement,"
            + tables[Album_Cache_int][1]
            + " text not null, "
            + tables[Album_Cache_int][2]
            + " text not null, "
            + tables[Album_Cache_int][3] + " text not null);";

    private static final String TABLE_PLAYLIST_CACHE_CREATE = "create table IF NOT EXISTS "
            + Playlist_Cache_table
            + "("
            + tables[Playlist_Cache_int][0]
            + " integer primary key autoincrement,"
            + tables[Playlist_Cache_int][1]
            + " text not null, "
            + tables[Playlist_Cache_int][2]
            + " text not null, "
            + tables[Playlist_Cache_int][3] + " text not null);";

    private static final String TABLE_V_PLAYLIST_CACHE_CREATE = "create table IF NOT EXISTS "
            + Video_Playlist_Cache_table
            + "("
            + tables[Video_Playlist_Cache_int][0]
            + " integer primary key autoincrement,"
            + tables[Video_Playlist_Cache_int][1]
            + " text not null, "
            + tables[Video_Playlist_Cache_int][2]
            + " text not null, "
            + tables[Video_Playlist_Cache_int][3] + " text not null);";

    private static final String TABLE_TRACKLIST_CREATE = "create table IF NOT EXISTS "
            + Tracklist_table
            + "("
            + tables[Tracklist_int][0]
            + " integer primary key autoincrement,"
            + tables[Tracklist_int][1]
            + " text not null, "
            + tables[Tracklist_int][2]
            + " text not null, "
            + tables[Tracklist_int][3]
            + " text not null);";

    private static final String TABLE_VIDEO_ID_LIST_CREATE = "create table IF NOT EXISTS "
            + VideoIdList_table
            + "("
            + tables[VideoIdList_int][0]
            + " integer primary key autoincrement,"
            + tables[VideoIdList_int][1]
            + " text not null, "
            + tables[VideoIdList_int][2]
            + " text not null, "
            + tables[VideoIdList_int][3]
            + " text not null);";

    private static final String TABLE_VIDEO_TRACK_CACHE_CREATE = "create table IF NOT EXISTS "
            + Video_Track_Cache_table
            + "("
            + tables[Video_Track_Cache_int][0]
            + " integer primary key autoincrement,"
            + tables[Video_Track_Cache_int][1]
            + " text not null, "
            + tables[Video_Track_Cache_int][2]
            + " text not null, "
            + tables[Video_Track_Cache_int][3]
            + " text not null, "
            + tables[Video_Track_Cache_int][4]
            + " text not null, "
            + tables[Video_Track_Cache_int][5]
            + " integer default 0, "
            + tables[Video_Track_Cache_int][6] + " text default 0, "
            + tables[Video_Track_Cache_int][7] + " text default 0);";

    private static final String TABLE_SONGCATCHER_HISTORY_CREATE = "create table IF NOT EXISTS "
            + SongCatcher_History_table
            + "("
            + tables[SongCatcher_History_int][0]
            + " integer primary key autoincrement,"
            + tables[SongCatcher_History_int][1]
            + " text not null, "
            + tables[SongCatcher_History_int][2]
            + " text not null, "
            + tables[SongCatcher_History_int][3]
            + " text not null, "
            + tables[SongCatcher_History_int][4] + " text not null);";

    private static final String TABLE_ALL_STRINGS_CREATE = "create table IF NOT EXISTS "
            + All_String_Values_table
            + "("
            + tables[All_Strings_int][0]
            + " integer primary key autoincrement,"
            + tables[All_Strings_int][1]
            + " text not null, "
            + tables[All_Strings_int][2]
            + " text not null, "
            + tables[All_Strings_int][3] + " text not null);";


    private static final String TABLE_DETAILS_CREATE = "create table IF NOT EXISTS "
            + Detail_Values_table
            + "("
            + tables[Detail_int][0]
            + " integer primary key autoincrement,"
            + tables[Detail_int][1]
            + " text not null, "
            + tables[Detail_int][2]
            + " text not null, "
            + tables[Detail_int][3]
            + " text not null, "
            + tables[Detail_int][4]
            + " text not null, "
            + tables[Detail_int][5] + " text not null);";

    private static final String TABLE_FAVORITE_LIST_CREATE = "create table IF NOT EXISTS "
            + FavoriteList_table
            + "("
            + tables[FavoriteList_int][0]
            + " integer primary key autoincrement,"
            + tables[FavoriteList_int][1]
            + " text not null, "
            + tables[FavoriteList_int][2]
            + " text not null, "
            + tables[FavoriteList_int][3]
            +  " text not null);";

    /** Constructor */
    private DataBase(Context ctx) {
//		HCtx = ctx;
        dbHelper = new DatabaseHelper(ctx);
    }

    public DataBase open() throws SQLException {
        try {
            sqLiteDb = dbHelper.getWritableDatabase();
        } catch (Error e) {
            //sqLiteDb = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            //sqLiteDb = dbHelper.getWritableDatabase();
        }
        return this;
    }

    public void clean() {
        try {
            sqLiteDb.delete(Track_Cache_table, null, null);
            sqLiteDb.delete(Tracklist_table, null, null);
            sqLiteDb.delete(VideoIdList_table, null, null);
            sqLiteDb.delete(Album_Cache_table, null, null);
            sqLiteDb.delete(Playlist_Cache_table, null, null);
            sqLiteDb.delete(Video_Playlist_Cache_table, null, null);
            sqLiteDb.delete(Video_Track_Cache_table, null, null);
            sqLiteDb.delete(SongCatcher_History_table, null, null);
            sqLiteDb.delete(All_String_Values_table, null, null);
            sqLiteDb.delete(Detail_Values_table, null, null);
            sqLiteDb.delete(FavoriteList_table, null, null);
//			sqLiteDb.delete(MEDIA_CONSUMPTION_TABLE, null, null);
        } catch (Exception e) {
        } catch (Error e) {
            System.gc();
            System.runFinalization();
            System.gc();
        }
    }

    public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
                                     String where) throws SQLException {
        try {
            CommonUtils.INSTANCE.setLog("downloadMigration", "fetch-"+"DATABASE_TABLE:-"+DATABASE_TABLE+"-tableNo:-"+tableNo+"-where:-"+where);
            Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
                    null, null, null, null);
            if (ret != null) {
                ret.moveToFirst();
            }
            CommonUtils.INSTANCE.setLog("downloadMigration", "fetch-"+ret);
            return ret;
        } catch (Exception e) {
            CommonUtils.INSTANCE.setLog("downloadMigration", "fetch-Exception-"+e.getMessage());
            return null;
        }
    }

    public synchronized long insert(String DATABASE_TABLE, int tableNo,
                                    String[] values) {
        ContentValues vals = new ContentValues();
        for (int i = 0; i < values.length; i++) {
            vals.put(tables[tableNo][i + 1], values[i]);
        }
        return sqLiteDb.insert(DATABASE_TABLE, null, vals);
    }

    public void cleanTable(int tableNo) {
        switch (tableNo) {
            case Track_Cache_int:
                sqLiteDb.delete(Track_Cache_table, null, null);
                break;
            case All_Strings_int:
                sqLiteDb.delete(All_String_Values_table, null, null);
                break;
            case Detail_int:
                sqLiteDb.delete(Detail_Values_table, null, null);
                break;
            case FavoriteList_int:
                sqLiteDb.delete(FavoriteList_table, null, null);
                break;
            default:
                break;
        }
    }

    public void close() {
        dbHelper.close();
    }


    public synchronized Cursor fetch(String DATABASE_TABLE, int tableNo,
                                     String where, String orderBy) throws SQLException {

        Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
                null, null, null, orderBy);
        if (ret != null) {
            ret.moveToFirst();
        }
        return ret;
    }

    public synchronized Cursor fetchAll(String DATABASE_TABLE, int tableNo) {
        try {
            return sqLiteDb.query(DATABASE_TABLE, tables[tableNo], null, null,
                    null, null, null);

        } catch (Exception e) {
            //Logger.e("fetchAll", e.getMessage());
            return null;
        }
    }

}

