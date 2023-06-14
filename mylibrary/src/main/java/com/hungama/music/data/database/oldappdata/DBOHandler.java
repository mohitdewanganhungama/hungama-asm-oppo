package com.hungama.music.data.database.oldappdata;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.hungama.music.utils.CommonUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DBOHandler {

    private static final Object mDBOHandlerMutext = new Object();
    private static final Object mNewDBOHandlerMutext = new Object();

    public static boolean insertTrackToCache(Context context, String id,
                                             String path, String jsonResponse, String state, String album_id,
                                             String playlist_id) {
        //DownloadStateManager.setDownloadState(id, DataBase.CacheState.getCacheStateByName(state));
        synchronized (mDBOHandlerMutext) {
            if (!isTrackExist(context, id)) {
                DataBase db = DataBase.getInstance(context);
                db.open();
                long rowId = db.insert(DataBase.Track_Cache_table,
                        DataBase.Track_Cache_int, new String[]{id, path,
                                jsonResponse, state, "0"});
                db.close();
                //printLog("insertTrackToCache ::::: " + rowId);
                return rowId != -1;
            }
            return false;
        }
    }

    public static boolean isTrackExist(Context context, String id) {
        synchronized (mDBOHandlerMutext) {
            boolean isExist = false;
            DataBase db = DataBase.getInstance(context);
            db.open();
            Cursor cursor = db.fetch(DataBase.Track_Cache_table,
                    DataBase.Track_Cache_int,
                    DataBase.tables[DataBase.Track_Cache_int][1] + "=" + id);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    isExist = true;
                cursor.close();
            }
            db.close();
            //printLog("isTrackExist ::::: " + isExist);
            return isExist;
        }
    }

    public static List<MediaItemOldApp> getAllCachedTracks(Context context) {
        DataBase db = DataBase.getInstance(context);
        db.open();

        Cursor cursor = db.fetch(DataBase.Track_Cache_table,
                DataBase.Track_Cache_int,
                DataBase.tables[DataBase.Track_Cache_int][4] + "='"
                        + "CACHED" + "'",
                DataBase.tables[DataBase.Track_Cache_int][7] + " ASC, " +
                        DataBase.tables[DataBase.Track_Cache_int][0] + " ASC");

        List<MediaItemOldApp> mediaItems = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int srNo = cursor.getInt(0);
                    String trackId = cursor.getString(1);
                    String filePath = cursor.getString(2);
                    String response = cursor.getString(3);
                    String state = cursor.getString(4);
                    String progress = cursor.getString(5);
                    String downloadRef = cursor.getString(6);
                    String timeStamp = cursor.getString(7);

                    MediaItemOldApp mediaItem = new MediaItemOldApp(srNo, trackId, filePath, response, state, progress, downloadRef, timeStamp);
                    mediaItems.add(mediaItem);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return mediaItems;
    }

    public static List<MediaItemOldApp> getAllCachedVideoTracks(Context context) {
        DataBase db = DataBase.getInstance(context);
        db.open();

        Cursor cursor = db.fetch(DataBase.Video_Track_Cache_table,
                DataBase.Video_Track_Cache_int,
                DataBase.tables[DataBase.Track_Cache_int][4] + "='"
                        + "CACHED" + "'",
                DataBase.tables[DataBase.Track_Cache_int][7] + " ASC, " +
                        DataBase.tables[DataBase.Track_Cache_int][0] + " ASC");

        List<MediaItemOldApp> mediaItems = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int srNo = cursor.getInt(0);
                    String trackId = cursor.getString(1);
                    String filePath = cursor.getString(2);
                    String response = cursor.getString(3);
                    String state = cursor.getString(4);
                    String progress = cursor.getString(5);
                    String downloadRef = cursor.getString(6);
                    String timeStamp = cursor.getString(7);

                    MediaItemOldApp mediaItem = new MediaItemOldApp(srNo, trackId, filePath, response, state, progress, downloadRef, timeStamp);
                    mediaItems.add(mediaItem);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return mediaItems;
    }

    public static List<PlaylistDetailModelOldApp> getCachedPlaylistsList(Context context) {
        synchronized (mDBOHandlerMutext) {
            List<PlaylistDetailModelOldApp> mediaItems = new ArrayList<>();
            try {
                // System.out.println(" ::::::::::::::::::::::: 1");
                DataBase db = DataBase.getInstance(context);
                db.open();
                Cursor cursor = db.fetchAll(DataBase.Playlist_Cache_table,
                        DataBase.Playlist_Cache_int);
                if (cursor != null) {
                    // System.out.println(" ::::::::::::::::::::::: 2");
                    if (cursor.moveToFirst()) {
                        Gson gson = new Gson();
                        PlaylistDetailModelOldApp playlistDetailModelOldApp;
                        do {
                            playlistDetailModelOldApp = gson.fromJson(cursor.getString(2), PlaylistDetailModelOldApp.class);
                            if (playlistDetailModelOldApp != null) {
                                mediaItems.add(playlistDetailModelOldApp);
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                db.close();
                Collections.reverse(mediaItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mediaItems;
        }
    }

    public static String getPlaylistIdFromTrackListTable(Context context, String track_id) {
        int count = 0;
        String playlistId = "";
        CommonUtils.INSTANCE.setLog("downloadMigration", "getPlaylistIdFromTrackListTable-track_id-"+track_id);
        if (!track_id.equals("0")) {
            DataBase db = DataBase.getInstance(context);
            db.open();
            Cursor cursor = db.fetch(
                    DataBase.Tracklist_table,
                    DataBase.Tracklist_int,
                    DataBase.tables[DataBase.Tracklist_int][1]
                            + " = '" + track_id + "'");
            CommonUtils.INSTANCE.setLog("downloadMigration", "getPlaylistIdFromTrackListTable-cursor-"+cursor);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        playlistId = cursor.getString(3);
                        CommonUtils.INSTANCE.setLog("downloadMigration", "getPlaylistIdFromTrackListTable-playListId-"+playlistId);
                        if (!TextUtils.isEmpty(playlistId) && !playlistId.equals("0")){
                            break;
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            db.close();
        }
        return playlistId;
    }

    public static PlaylistDetailModelOldApp getDataFromPlayListCacheTable(Context context, String playlistId) {
        synchronized (mDBOHandlerMutext) {
            PlaylistDetailModelOldApp playlistDetailModelOldApp = new PlaylistDetailModelOldApp();
            try {
                // System.out.println(" ::::::::::::::::::::::: 1");
                DataBase db = DataBase.getInstance(context);
                db.open();
                Cursor cursor = db.fetch(
                        DataBase.Playlist_Cache_table,
                        DataBase.Playlist_Cache_int,
                        DataBase.tables[DataBase.Playlist_Cache_int][1]
                                + " = '" + playlistId + "'");
                if (cursor != null) {
                    // System.out.println(" ::::::::::::::::::::::: 2");
                    if (cursor.moveToFirst()) {
                        Gson gson = new Gson();
                        do {
                            playlistDetailModelOldApp = gson.fromJson(cursor.getString(2), PlaylistDetailModelOldApp.class);
                            if (playlistDetailModelOldApp != null && !TextUtils.isEmpty(String.valueOf(playlistDetailModelOldApp.getContentId()))) {
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return playlistDetailModelOldApp;
        }
    }

    public static String getAlbumIdFromTrackListTable(Context context, String track_id) {
        int count = 0;
        String albumId = "";
        CommonUtils.INSTANCE.setLog("downloadMigration", "getAlbumIdFromTrackListTable-track_id-"+track_id);
        if (!track_id.equals("0")) {
            DataBase db = DataBase.getInstance(context);
            db.open();
            Cursor cursor = db.fetch(
                    DataBase.Tracklist_table,
                    DataBase.Tracklist_int,
                    DataBase.tables[DataBase.Tracklist_int][1]
                            + " = '" + track_id + "'");
            CommonUtils.INSTANCE.setLog("downloadMigration", "getAlbumIdFromTrackListTable-cursor-"+cursor);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        albumId = cursor.getString(2);
                        CommonUtils.INSTANCE.setLog("downloadMigration", "getAlbumIdFromTrackListTable-playListId-"+albumId);
                        if (!TextUtils.isEmpty(albumId) && !albumId.equals("0")){
                            break;
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            db.close();
        }
        return albumId;
    }

    public static PlaylistDetailModelOldApp getDataFromAlbumCacheTable(Context context, String albumId) {
        synchronized (mDBOHandlerMutext) {
            PlaylistDetailModelOldApp albumDetailModelOldApp = new PlaylistDetailModelOldApp();
            try {
                // System.out.println(" ::::::::::::::::::::::: 1");
                DataBase db = DataBase.getInstance(context);
                db.open();
                Cursor cursor = db.fetch(
                        DataBase.Album_Cache_table,
                        DataBase.Album_Cache_int,
                        DataBase.tables[DataBase.Album_Cache_int][1]
                                + " = '" + albumId + "'");
                if (cursor != null) {
                    // System.out.println(" ::::::::::::::::::::::: 2");
                    if (cursor.moveToFirst()) {
                        Gson gson = new Gson();
                        do {
                            albumDetailModelOldApp = gson.fromJson(cursor.getString(2), PlaylistDetailModelOldApp.class);
                            if (albumDetailModelOldApp != null && !TextUtils.isEmpty(String.valueOf(albumDetailModelOldApp.getContentId()))) {
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return albumDetailModelOldApp;
        }
    }
}

