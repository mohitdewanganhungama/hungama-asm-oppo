package com.hungama.music.data.database.oldappdata;

public class MediaItemOldApp {
    private long id;
    private String trackId;
    private String filePath;
    private String jsonDetails;
    private String state;
    private String progress;
    private String downloadRef;
    private String timeStamp;

    private String trackListTrackId;
    private String trackListAlbumId;
    private String trackListPlaylistId;

    public MediaItemOldApp(long id, String trackId, String filePath, String jsonDetails, String state, String progress, String downloadRef, String timeStamp) {
        this.id = id;
        this.trackId = trackId;
        this.filePath = filePath;
        this.jsonDetails = jsonDetails;
        this.state = state;
        this.progress = progress;
        this.downloadRef = downloadRef;
        this.timeStamp = timeStamp;
    }

    public MediaItemOldApp(String trackListTrackId, String trackListAlbumId, String trackListPlaylistId){
        this.trackListTrackId = trackListTrackId;
        this.trackListAlbumId = trackListAlbumId;
        this.trackListPlaylistId = trackListPlaylistId;
    }

    public long getId() {
        return id;
    }

    public String getTrackId() {
        return trackId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getJsonDetails() {
        return jsonDetails;
    }

    public String getState() {
        return state;
    }

    public String getProgress() {
        return progress;
    }

    public String getDownloadRef() {
        return downloadRef;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getTrackListTrackId() {
        return trackListTrackId;
    }

    public String getTrackListAlbumId() {
        return trackListAlbumId;
    }

    public String getTrackListPlaylistId() {
        return trackListPlaylistId;
    }
}

