package com.hungama.music.player.audioplayer.lyrics;

public class Lrc {
    private long time;
    private String text;

    public void setTime(long time) {
        this.time = time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

}