package com.hungama.music.player.audioplayer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.hungama.music.player.audioplayer.model.Track

interface TracksContract {
    interface View {

        fun startTrackPlayback(
            selectedTrackPosition: Int,
            tracksList: MutableList<Track>,
            trackPlayStartPosition: Long
        )

        fun getViewActivity(): AppCompatActivity

        fun getApplicationContext(): Context
    }

    interface Presenter {

        fun prepareTrackPlayback(selectedTrackPosition: Int, trackPlayStartPosition:Long = 0)

        fun setNowPlayingTrack(trackId: Long)

        fun onCleanup()

    }
}