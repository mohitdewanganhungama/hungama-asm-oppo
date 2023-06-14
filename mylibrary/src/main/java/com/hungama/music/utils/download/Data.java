package com.hungama.music.utils.download;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.hungama.fetch2.Priority;
import com.hungama.fetch2.Request;

import java.util.ArrayList;
import java.util.List;

public final class Data {

    public static String[] sampleUrls = new String[]{
            /*"http://speedtest.ftp.otenet.gr/files/test100Mb.db",
            "https://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_720p_stereo.avi",
            "http://stg-api.test.hungamagames.com/master/tempfiles/Wafa-Na-Raas-Aayee.lrc",
            "http://stg-api.test.hungamagames.com/master/tempfiles/Jubin.jpg",
            "https://images.hungama.com/a/1/f5e/536/6793/6793_175x175.jpg",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",*/
    "http://test.com"};

    private Data() {

    }

    @NonNull
    private static List<Request> getFetchRequests(Context context) {
        final List<Request> requests = new ArrayList<>();
        for (String sampleUrl : sampleUrls) {
            final Request request = new Request(sampleUrl, getFilePath(sampleUrl, context));
            requests.add(request);
        }
        return requests;
    }

    @NonNull
    public static List<Request> getFetchRequestWithGroupId(final int groupId, Context context) {
        final List<Request> requests = getFetchRequests(context);
        for (Request request : requests) {
            request.setGroupId(groupId);
        }
        return requests;
    }

    @NonNull
    private static String getFilePath(@NonNull final String url, Context context) {
        final Uri uri = Uri.parse(url);
        final String fileName = uri.getLastPathSegment();
        final String dir = getSaveDir(context);
        return (dir + "/DownloadList/" + fileName);
    }

    @NonNull
    public static String getNameFromUrl(final String url) {
        return Uri.parse(url).getLastPathSegment();
    }

    @NonNull
    public static List<Request> getGameUpdates(Context context) {
        final List<Request> requests = new ArrayList<>();
        final String url = "http://speedtest.ftp.otenet.gr/files/test100k.db";
        for (int i = 0; i < 10; i++) {
            final String filePath = getSaveDir(context) + "/gameAssets/" + "asset_" + i + ".asset";
            final Request request = new Request(url, filePath);
            request.setPriority(Priority.HIGH);
            requests.add(request);
        }
        return requests;
    }

    @NonNull
    public static String getSaveDir(Context context) {
        //return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/fetch";
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
    }

    @NonNull
    public static String getSaveAudioDir(Context context) {
        return context.getExternalCacheDir().getParentFile()+ "/files/track/" ;
    }

    @NonNull
    public static String getSaveVideoDir(Context context) {
        return context.getExternalCacheDir().getParentFile()+ "/files/video/" ;
    }

    /*@NonNull
    public static String getSaveVideoDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
    }*/

    @NonNull
    public static String getSaveAudioFileName(String name){
        return "audio_"+name+".cache";
    }

    @NonNull
    public static String getSaveAudioThumbnailFileName(String name){
        return "audio_"+name+".thbn";
    }
    @NonNull
    public static String getSaveAudioParentThumbnailFileName(String name){
        return "audio_parent_"+name+".thbn";
    }

    @NonNull
    public static String getSaveAudioLrcFileName(String name){
        return "audio_lrc_"+name;
    }

}
